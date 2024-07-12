package com.harana.modules

import com.google.common.base.Strings
import com.harana.modules.core.app.App.runEffect
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.vertx.models._
import com.harana.modules.vertx.models.streams.{BufferReadStream, GzipReadStream, InputStreamReadStream, Pump}
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.http._
import io.vertx.core.{AsyncResult, Handler, MultiMap, Promise, Vertx => VX}
import io.vertx.ext.reactivestreams.ReactiveWriteStream
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import io.vertx.ext.web.{Router, RoutingContext}
import org.apache.commons.io.FilenameUtils
import org.apache.logging.log4j.LogManager
import org.pac4j.core.config.{Config => Pac4jConfig}
import org.pac4j.core.context.session.SessionStore
import org.pac4j.vertx.auth.Pac4jAuthProvider
import org.pac4j.vertx.handler.impl.{SecurityHandler, SecurityHandlerOptions}
import zio.internal.Platform
import zio.{Runtime, Unsafe, ZEnvironment, ZIO}

import java.io.{File, FileInputStream}
import java.nio.file.Path
import scala.jdk.CollectionConverters._

package object vertx {

  type Address = String
  type WebSocketHeaders = MultiMap

  val logger = LogManager.getLogger("Vertx")
  val corsRules = CrossOriginResourceSharing()

  def generateResponse(vx: VX,
                       logger: Logger,
                       micrometer: Micrometer,
                       templateEngine: HandlebarsTemplateEngine,
                       uploadsDirectory: Path,
                       rc: RoutingContext,
                       handler: RouteHandler,
                       secured: Boolean = false): Unit =
    runEffect(
      for {
        sample        <- micrometer.startTimer
        _             <- logger.info(s"${rc.request().method().name()}: ${rc.request().uri()}")
        _             <- logger.debug(rc.request().headers().asScala.map { e => s"${e.getKey} - ${e.getValue}"}.mkString("\n\t\t"))
        handler       <- handler match {
                          case RouteHandler.Standard(handler) =>
                            handler(rc)

                          case RouteHandler.FileUpload(handler) =>
                            for {
                              _             <- ZIO.succeed(rc.request().setExpectMultipart(true))
                              handler       <- handler(rc, uploadsDirectory, rc.fileUploads().asScala.toList)
                            } yield handler

                          // Need to handle logic from here: https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/io/vertx/ext/web/handler/impl/BodyHandlerImpl.java
                          case RouteHandler.Stream(handler) =>
                            for {
                              stream    <- ZIO.succeed(ReactiveWriteStream.writeStream[Buffer](vx))
                              pump      =  Pump(rc.request(), stream)
                              handler   <- handler(rc, stream, pump)
                            } yield handler
                        }

        _             = handler match {
                          case  Response.Buffer(buffer, gzipped, _, _, _, _, _) =>
                            val brs = new BufferReadStream(buffer)
                            val rs = if (gzipped) new GzipReadStream(brs) else brs
                            val pump = Pump(rs, rc.response())
                            rs.endHandler(_ => rc.response().close())
                            pump.start()

                          case Response.Content(content, contentType, cookies, statusCode, cors, headers) =>
                            response(rc, contentType, cookies, statusCode, cors, headers).end(content)

                          case Response.Empty(contentType, cookies, statusCode, cors, headers) =>
                            response(rc, contentType, cookies, statusCode, cors, headers).end()

                          case Response.File(filename, inputStream, gzipped, contentSize, contentType, cookies, statusCode, cors, headers) =>
                            val r = response(rc, contentType, cookies, statusCode, cors, headers)
                            r.putHeader("Content-Disposition",  s"attachment; filename=$filename;")
                            r.setChunked(true)
                            if (contentSize.nonEmpty) r.putHeader(HttpHeaders.CONTENT_LENGTH, contentSize.get.toString)
                            val isrs = new InputStreamReadStream(inputStream, vx)
                            val rs = if (gzipped) new GzipReadStream(isrs) else isrs
                            val pump = Pump(rs, r)
                            rs.endHandler(_ => r.end().onComplete((_: AsyncResult[Void]) => r.close()))
                            pump.start()

                          case Response.InputStream(inputStream, gzipped, contentSize, contentType, cookies, statusCode, cors, headers) =>
                            val r = response(rc, contentType, cookies, statusCode, cors, headers)
                            r.setChunked(true)
                            val isrs = new InputStreamReadStream(inputStream, vx)
                            if (contentSize.nonEmpty) r.putHeader(HttpHeaders.CONTENT_LENGTH, contentSize.get.toString)
                            val rs = if (gzipped) new GzipReadStream(isrs) else isrs
                            rs.endHandler(_ => r.end().onComplete((_: AsyncResult[Void]) => r.close()))
                            Pump(rs, r).start()

                          case Response.JSON(json, contentType, cookies, statusCode, cors, headers) =>
                            response(rc, contentType, cookies, statusCode, cors, headers).end(json.toString)

                          case Response.ReadStream(stream, contentSize, contentType, cookies, statusCode, cors, headers) =>
                            val r = response(rc, contentType, cookies, statusCode, cors, headers)
                            r.setChunked(true)
                            if (contentSize.nonEmpty) r.putHeader(HttpHeaders.CONTENT_LENGTH, contentSize.get.toString)
                            stream.endHandler(_ => r.end().onComplete((_: AsyncResult[Void]) => r.close()))
                            Pump(stream, r).start()

                          case Response.Redirect(url, contentType, cookies, _, cors, headers) =>
                            response(rc, contentType, cookies, Some(302), cors, headers).putHeader("location", url).end()

                          case Response.Template(path, parameters, contentType, cookies, statusCode, cors, headers) =>
                            templateEngine.render(parameters.asJava, path, new Handler[AsyncResult[Buffer]] {
                              override def handle(result: AsyncResult[Buffer]): Unit =
                                if (result.succeeded()) response(rc, contentType, cookies, statusCode, cors, headers).end(result.result())
                                else {
                                  result.cause().printStackTrace()
                                  rc.fail(result.cause())
                                }
                            })
                        }
        _           <-  micrometer.stopTimer(sample, s"route_${rc.normalizedPath().substring(1).replaceAll("/", "_")}")
      } yield ()
    )


  def anonymousAuth(vx: VX,
                    sessionStore: SessionStore,
                    config: Pac4jConfig,
                    authProvider: Pac4jAuthProvider,
                    url: String,
                    router: Router) = {

    val options = new SecurityHandlerOptions().setClients("AnonymousClient")
    router.get(url).handler(new SecurityHandler(vx, sessionStore, config, authProvider, options))
  }


  def sendFile(file: File, vx: VX, rc: RoutingContext) = {
    val r = rc.response()
    r.putHeader("Content-Disposition", s"attachment; filename=${file.getName};")
    val extension = FilenameUtils.getExtension(file.getName)
    if (MimeTypes.all.contains(extension))
      r.putHeader("Content-Type", MimeTypes.all(extension))
    r.setChunked(true)
    r.putHeader(HttpHeaders.CONTENT_LENGTH, file.length().toString)
    val rs = new InputStreamReadStream(new FileInputStream(file), vx)
    val pump = Pump(rs, r)
    rs.endHandler(_ => {
      r.end()
      r.close()
    })
    pump.start()
  }

  def auth(vx: VX,
           sessionStore: SessionStore,
           config: Pac4jConfig,
           authProvider: Pac4jAuthProvider,
           router: Router,
           url: String,
           handler: Handler[RoutingContext],
           clientNames: String,
           authorizerName: Option[String]): Unit = {

    var options = new SecurityHandlerOptions().setClients(clientNames)
    if (authorizerName.nonEmpty) options = options.setAuthorizers(authorizerName.get)
    router.get(url).handler(new SecurityHandler(vx, sessionStore, config, authProvider, options))
    router.get(url).handler(setContentType(ContentType.HTML.value))
    router.get(url).handler(handler)
  }


  def setContentType(contentType: String): Handler[RoutingContext] =
    (rc: RoutingContext) => {
      rc.response.putHeader(CONTENT_TYPE, contentType)
      rc.next()
    }

// FIXME
//  def toHandler[R, A](runtime: Runtime[_], zio: ZIO[R, _, A], environment: ZEnvironment[R]) =
//    (p: Promise[A]) => p.complete(
//      Unsafe.unsafe { implicit unsafe =>
//        Runtime.default.unsafe.run(zio.provideEnvironment(environment)).getOrThrow()
//      }
//    )


  def getVersion = {
    var version = System.getProperty("java.version")
    if (version.startsWith("1.")) version = version.substring(2, 3)
    else {
      val dot = version.indexOf(".")
      if (dot != -1) version = version.substring(0, dot)
    }
    version.toInt
  }

  def defaultAllowedHeaders =
    Set(
      "x-requested-with",
      "Access-Control-Allow-Origin",
      "Access-Control-Allow-Methods",
      "Access-Control-Allow-Headers",
      "Access-Control-Allow-Credentials",
      "origin",
      "Content-Type",
      "accept",
      "Authorization")

  def defaultAllowedMethods =
    Set(
      HttpMethod.GET,
      HttpMethod.POST,
      HttpMethod.OPTIONS,
      HttpMethod.DELETE,
      HttpMethod.PATCH,
      HttpMethod.PUT)

  def syncSockets(source: ServerWebSocket, target: WebSocket) = {
    syncSocket(source, target)
    syncSocket(target, source)
  }

  private def syncSocket(first: WebSocketBase, second: WebSocketBase) = {
    if (!first.isClosed) {
      first.frameHandler(frame => {
        if (frame.isClose) second.close()
        if (frame.isContinuation) second.writeFrame(WebSocketFrame.continuationFrame(frame.binaryData(), frame.isFinal))

        (frame.isFinal, frame.isBinary, frame.isText) match {
          case (true, true, _) => second.writeFinalBinaryFrame(frame.binaryData())
          case (true, _, true) => second.writeFinalTextFrame(frame.textData())
          case (false, true, _) => second.writeBinaryMessage(frame.binaryData())
          case (false, _, true) => second.writeTextMessage(frame.textData())
          case (_, _, _) =>
        }
      })
    }
  }

  private def response(rc: RoutingContext,
                       contentType: Option[ContentType],
                       cookies: List[Cookie],
                       statusCode: Option[Int],
                       cors: Boolean,
                       headers: Map[_ <: CharSequence, List[_ <: CharSequence]]) = {
    val response = rc.response()
    if (contentType.nonEmpty) response.putHeader(CONTENT_TYPE, contentType.get.value)
    cookies.foreach(response.addCookie)
    if (statusCode.nonEmpty) response.setStatusCode(statusCode.get)
    if (cors) {
      val corsOrigin = rc.request().getHeader(HttpHeaders.ORIGIN)
      if (!Strings.isNullOrEmpty(corsOrigin) && corsRules.isOriginAllowed(corsOrigin)) {
        response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.toString, corsRules.getAllowedOrigin(corsOrigin))
        response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS.toString, corsRules.allowedMethods.asJava)
      }
    }
    headers.foreach { case (k, v) =>
      if (v.size == 1) response.putHeader(k.toString, v.head.toString)
      else response.putHeader(k.toString, v.map(_.toString).asJava)
    }
    response
  }
}