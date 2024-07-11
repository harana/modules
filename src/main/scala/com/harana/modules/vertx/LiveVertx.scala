package com.harana.modules.vertx

import com.harana.modules.core.app.App.runEffect
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.vertx.gc.GCHealthCheck
import com.harana.modules.vertx.models._
import com.harana.modules.vertx.proxy.{WSURI, WebProxyClient, WebProxyClientOptions}
import io.vertx.core.eventbus._
import io.vertx.core.file.FileSystemOptions
import io.vertx.core.http.{HttpServer, HttpServerOptions, WebSocket}
import io.vertx.core.json.JsonObject
import io.vertx.core.net.{JksOptions, NetServer, NetServerOptions}
import io.vertx.core.shareddata.{AsyncMap, Counter, Lock}
import io.vertx.core.{AsyncResult, Context, Handler, VertxOptions, Vertx => VX}
import io.vertx.ext.bridge.{BridgeOptions, PermittedOptions}
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge
import io.vertx.ext.web.client.{WebClient, WebClientOptions}
import io.vertx.ext.web.handler.{BodyHandler, CorsHandler, SessionHandler}
import io.vertx.ext.web.sstore.cookie.CookieSessionStore
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import io.vertx.ext.web.{Router, RoutingContext}
import io.vertx.micrometer.{MicrometerMetricsOptions, PrometheusScrapingHandler, VertxPrometheusOptions}
import io.vertx.servicediscovery.{Record, ServiceDiscovery}
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import org.jose4j.jwk.JsonWebKeySet
import org.pac4j.core.client.Clients
import org.pac4j.core.config.{Config => Pac4jConfig}
import org.pac4j.core.profile.UserProfile
import org.pac4j.vertx.context.session.VertxSessionStore
import org.pac4j.vertx.handler.impl._
import org.pac4j.vertx.http.VertxHttpActionAdapter
import org.pac4j.vertx.{VertxProfileManager, VertxWebContext}
import zio.{Task, UIO, ZIO, ZLayer}

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import scala.collection.concurrent.{TrieMap, Map => ConcurrentMap}
import scala.compat.java8.FunctionConverters.asJavaFunction
import scala.compat.java8.OptionConverters._
import scala.jdk.CollectionConverters._

object LiveVertx {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveVertx(config, logger, micrometer)
  }
}

case class LiveVertx(config: Config, logger: Logger, micrometer: Micrometer) extends Vertx {

  System.setProperty("org.jboss.logging.provider", "log4j2")
  System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")

  private val vertxRef = new AtomicReference[VX](null)
  private val serviceDiscoveryRef = new AtomicReference[Option[ServiceDiscovery]](None)
  private val serviceDiscoveryListeners: ConcurrentMap[String, Record => Unit] = TrieMap.empty

  private def vertx(clustered: Boolean) =
    for {
      vertxBlockedThreads     <- config.long("vertx.blockedThreadsCheckInterval", 10000L)

      zookeeperHost           <- config.optSecret("zookeeper-host")
      zookeeperPrefix         <- config.optString("zookeeper.prefix")

      listenHost              <- config.string("http.listenHost")
      publicHost              <- config.string("http.publicHost", sys.env.getOrElse("POD_IP", listenHost))
      eventBusPort            <- config.int("http.eventBusPort", 10000)

      eventBusOptions         =  new EventBusOptions()
                                  .setClusterPublicHost(publicHost)
                                  .setClusterPublicPort(eventBusPort)
                                  .setLogActivity(true)

      fileSystemOptions       = new FileSystemOptions().setFileCachingEnabled(false)

      registry                <- micrometer.registry

      clusterManager          <- if (zookeeperHost.nonEmpty) ZIO.some {
                                  val zkConfig = new JsonObject()
                                  zkConfig.put("zookeeperHosts", zookeeperHost)
                                  zkConfig.put("rootPath", zookeeperPrefix.map(p => s"$p.vertx").getOrElse("vertx"))
                                  new ZookeeperClusterManager(zkConfig)
                                 } else ZIO.none

      vertxOptions            = new VertxOptions()
                                  .setBlockedThreadCheckInterval(vertxBlockedThreads)
                                  .setEventBusOptions(eventBusOptions)
                                  .setFileSystemOptions(fileSystemOptions)
                                  .setMetricsOptions(new MicrometerMetricsOptions()
                                    .setMicrometerRegistry(registry)
                                    .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true)).setEnabled(true)
                                  )

      vx                      <- ZIO.async { (cb: Task[VX] => Unit) =>
                                  if (clustered)
                                    VX.clusteredVertx(
                                      vertxOptions.setClusterManager(clusterManager.get),
                                      (result: AsyncResult[VX]) => if (result.succeeded()) cb(ZIO.succeed(result.result)) else cb(ZIO.fail(result.cause()))
                                    )
                                  else
                                    cb(ZIO.succeed(VX.vertx(vertxOptions)))
                                 }
    } yield vx


  private def serviceDiscovery: Task[ServiceDiscovery] =
    for {
      serviceDiscovery        <- if (serviceDiscoveryRef.get.nonEmpty) ZIO.attempt(serviceDiscoveryRef.get.get) else ZIO.attempt(ServiceDiscovery.create(vertxRef.get()))
      _                       =  serviceDiscoveryRef.set(Some(serviceDiscovery))
    } yield serviceDiscovery


  def underlying: UIO[VX] =
    ZIO.succeed(vertxRef.get)


  def subscribe(address: Address, `type`: String, onMessage: String => Task[Unit]): Task[MessageConsumer[String]] =
    for {
      result  <- ZIO.async { (cb: Task[MessageConsumer[String]] => Unit) =>
                    val consumer = vertxRef.get().eventBus.consumer(address, (message: Message[String]) => {
                      if (message.headers().get("type").equals(`type`)) {
                        val body = if (message.body() == null) null else new String(Base64.getDecoder.decode(message.body()))
                        runEffect(onMessage(body))
                      }}
                    )
                    consumer.completionHandler((result: AsyncResult[Void]) =>
                      if (result.succeeded()) cb(logger.debug(s"Subscribed to address: $address").as(consumer))
                      else cb(logger.error(s"Failed to subscribe to address: $address") *> ZIO.fail(result.cause()))
                    )
                }
    } yield result


  def unsubscribe(consumer: MessageConsumer[String]): Task[Unit] =
    for {
      result  <- ZIO.async { (cb: Task[Unit] => Unit) =>
                  consumer.unregister((result: AsyncResult[Void]) =>
                    if (result.succeeded()) cb(logger.debug(s"Unsubscribed from address: ${consumer.address()}").unit)
                    else cb(logger.error(s"Failed to unsubscribe from address: ${consumer.address()}") *> ZIO.fail(result.cause()))
                  )
                }
    } yield result


  def publishMessage(address: Address, `type`: String, message: String): Task[Unit] =
    for {
      m   <- ZIO.attempt(Base64.getEncoder.encode(message.getBytes("UTF-8")))
      _   <- ZIO.attempt(vertxRef.get().eventBus.publish(address, new String(m), new DeliveryOptions().addHeader("type", `type`)))
    } yield ()


  def publishMessage(address: Address, `type`: String): Task[Unit] =
    ZIO.attempt(vertxRef.get().eventBus.send(address, null, new DeliveryOptions().addHeader("type", `type`)))


  def sendMessage(address: Address, `type`: String, message: String): Task[Unit] =
    for {
      m   <- ZIO.attempt(Base64.getEncoder.encode(message.getBytes("UTF-8")))
      _   <- ZIO.attempt(vertxRef.get().eventBus.send(address, new String(m), new DeliveryOptions().addHeader("type", `type`)))
    } yield ()


  def sendMessage(address: Address, `type`: String): Task[Unit] =
    ZIO.attempt(vertxRef.get().eventBus.send(address, null, new DeliveryOptions().addHeader("type", `type`)))


  def service(name: String): Task[Option[Record]] =
    for {
      sd        <- serviceDiscovery
      fn        =  (record: Record) => Boolean.box(record.getName.equals(name))
      record    <- ZIO.async { (cb: Task[Option[Record]] => Unit) =>
                      sd.getRecord(asJavaFunction(fn), (result: AsyncResult[Record]) =>
                        if (result.succeeded()) cb(ZIO.succeed(Option(result.result()))) else cb(ZIO.fail(result.cause()))
                      )
                    }
    } yield record


  def services(filters: Map[String, String]): Task[List[Record]] =
    for {
      sd        <- serviceDiscovery
      json      =  new JsonObject()
      _         =  filters.foreach { case (k, v) => json.put(k, v) }
      record    <- ZIO.async { (cb: Task[List[Record]] => Unit) =>
                      sd.getRecords(json, (result: AsyncResult[java.util.List[Record]]) =>
                        if (result.succeeded()) cb(ZIO.succeed(result.result().asScala.toList)) else cb(ZIO.fail(result.cause()))
                      )
                    }
    } yield record


  def registerServiceListener(name: String, onChange: Record => Unit): UIO[Unit] =
    ZIO.succeed(serviceDiscoveryListeners.put(name, onChange))


  def deregisterServiceListener(name: String): UIO[Unit] =
    ZIO.succeed(serviceDiscoveryListeners.remove(name))


  def lock(name: String): Task[Lock] =
    ZIO.async { (cb: Task[Lock] => Unit) =>
      vertxRef.get().sharedData().getLock(name, (result: AsyncResult[Lock]) =>
        if (result.succeeded()) cb(ZIO.succeed(result.result())) else cb(ZIO.fail(result.cause()))
      )
     }


  def lockWithTimeout(name: String, timeoutSeconds: String, onLock: Lock => Task[Unit]): Task[Lock] =
    ZIO.async { (cb: Task[Lock] => Unit) =>
      vertxRef.get().sharedData().getLock(name, (result: AsyncResult[Lock]) =>
        if (result.succeeded()) cb(ZIO.succeed(result.result())) else cb(ZIO.fail(result.cause()))
      )
    }


  def getCounter(name: String): Task[Counter] =
    ZIO.async { (cb: Task[Counter] => Unit) =>
      vertxRef.get().sharedData().getCounter(name, (result: AsyncResult[Counter]) =>
        if (result.succeeded()) cb(ZIO.succeed(result.result())) else cb(ZIO.fail(result.cause()))
      )
    }


  private def withMap[K, V, X](name: String, fn: (AsyncMap[K, V], Handler[AsyncResult[X]]) => Unit): Task[X] =
    for {
      map           <- getMap[K, V](name)
      result        <- ZIO.async { (cb: Task[X] => Unit) =>
                        fn(map, result => if (result.succeeded()) cb(ZIO.succeed(result.result())) else cb(ZIO.fail(result.cause())))
      }
    } yield result


  def clearMap[K, V](name: String): Task[Unit] =
    withMap[K, V, Void](name, (map, handler) => map.clear(handler)).unit


  def getMap[K, V](name: String): Task[AsyncMap[K, V]] =
    ZIO.async { (cb: Task[AsyncMap[K, V]] => Unit) =>
      vertxRef.get().sharedData().getAsyncMap[K, V](name, (result: AsyncResult[AsyncMap[K, V]]) =>
          if (result.succeeded()) cb(ZIO.succeed(result.result())) else cb(ZIO.fail(result.cause()))
        )
      }


  def getMapKeys[K, V](name: String): Task[Set[K]] =
    withMap[K, V, java.util.Set[K]](name, (map, handler) => map.keys(handler)).map(_.asScala.toSet)


  def getMapValues[K, V](name: String): Task[List[V]] =
    withMap[K, V, java.util.List[V]](name, (map, handler) => map.values(handler)).map(_.asScala.toList)


  def getMapValue[K, V](name: String, key: K): Task[Option[V]] =
    withMap[K, V, V](name, (map, handler) => map.get(key, handler)).map(Option.apply)


  def putMapValue[K, V](name: String, key: K, value: V, ttl: Option[Long] = None): Task[Unit] =
    withMap[K, V, Void](name, (map, handler) => if (ttl.nonEmpty) map.put(key, value, ttl.get, handler) else map.put(key, value, handler)).unit


  def removeMapValue[K, V](name: String, key: K): Task[Unit] =
    withMap[K, V, Void](name, (map, _) => map.remove(key)).unit


  def putMapValueIfAbsent[K, V](name: String, key: K, value: V, ttl: Option[Long] = None): Task[V] =
    withMap[K, V, V](name, (map, handler) => if (ttl.nonEmpty) map.putIfAbsent(key, value, ttl.get, handler) else map.putIfAbsent(key, value, handler))


  def getOrCreateContext: UIO[Context] =
    ZIO.succeed(vertxRef.get().getOrCreateContext())


  def close: Task[Unit] =
    ZIO.attempt(vertxRef.get().close())


  def eventBus: UIO[EventBus] =
    ZIO.succeed(vertxRef.get().eventBus())


  def startHttpServer(domain: String,
                      proxyDomain: Option[String] = None,
                      routes: List[Route] = List(),
                      clustered: Boolean = false,
                      defaultHandler: Option[RouteHandler] = None,
                      proxyMapping: Option[RoutingContext => Task[Option[URI]]] = None,
                      webSocketProxyMapping: Option[WebSocketHeaders => Task[WSURI]] = None,
                      errorHandlers: Map[Int, RoutingContext => Task[Response]] = Map(),
                      eventBusInbound: List[String] = List(),
                      eventBusOutbound: List[String] = List(),
                      authTypes: List[AuthType] = List(),
                      additionalAllowedHeaders: Set[String] = Set(),
                      postLogin: Option[(RoutingContext, Option[UserProfile]) => Task[Response]] = None,
                      sessionRegexp: Option[String] = None,
                      jwtKeySet: Option[JsonWebKeySet] = None,
                      logActivity: Boolean = false): Task[HttpServer] =
    for {
      useSSL                <- config.boolean("http.useSSL", default = false)
      publicSSL             <- config.boolean("http.publicSSL", default = true)
      listenHost            <- config.string("http.listenHost", "127.0.0.1")
      listenPort            <- config.int("http.listenPort", 8082)
      publicHost            <- config.string("http.publicHost", listenHost)
      publicPort            <- config.int("http.publicPort", if (publicSSL) 443 else 80)
      keyStorePath          <- config.optString("http.keyStorePath")
      keyStorePassword      <- config.optPassword("http.keyStorePassword")
      proxyTimeout          <- config.long("http.proxyTimeout", 24 * 60 * 60)
      uploadsDirectory      <- config.path("http.uploadsDirectory", Files.createTempDirectory("harana"))

      publicUrl             =  if (publicSSL) s"""https://$domain${if (!publicPort.equals(443)) s":$publicPort" else ""}""" else s"""http://$domain${if (!publicPort.equals(80)) s":$publicPort" else ""}"""

      vx                    <- vertx(clustered)
      _                     =  vertxRef.set(vx)

      router                <- ZIO.succeed(Router.router(vx))

// FIXME: What is this for ?
//        _                     = router.route().handler((rc: RoutingContext) => {
//                                  rc.request().pause()
//                                  rc.next()
//                                })

      clusteredStore        <- ZIO.attempt(CookieSessionStore.create(vx, "temp"))
      sessionStore          <- ZIO.attempt(new VertxSessionStore(clusteredStore))
      sessionHandler        <- ZIO.attempt(SessionHandler.create(clusteredStore))
      templateEngine        <- ZIO.attempt(HandlebarsTemplateEngine.create(vx))
      webClient             <- ZIO.attempt(WebClient.create(vx, new WebClientOptions().setFollowRedirects(false).setMaxRedirects(1)))
      httpClient            <- ZIO.attempt(vx.createHttpClient())

      _                     <- ZIO.attempt {
                                // Custom Routes
                                routes.foreach { route =>

                                  def handler(rc: RoutingContext): Unit =
                                    generateResponse(vx, logger, micrometer, templateEngine, uploadsDirectory, rc, route.handler, route.secured)

                                  route.handler match {
                                    case RouteHandler.Standard(_) | RouteHandler.FileUpload(_) => router.route().handler(BodyHandler.create())
                                    case RouteHandler.Stream(_) => router.route().handler(rc => rc.request().pause())
                                  }

                                  if (route.regex) {
                                    if (route.blocking)
                                      router.routeWithRegex(route.method, route.path).virtualHost(domain).blockingHandler(handler)
                                    else
                                      router.routeWithRegex(route.method, route.path).virtualHost(domain).handler(handler)
                                  }
                                  else {
                                    val customRoute =
                                      if (route.blocking)
                                        router.route(route.method, route.path).virtualHost(domain).blockingHandler(handler).useNormalizedPath(route.normalisedPath)
                                      else
                                        router.route(route.method, route.path).virtualHost(domain).handler(handler).useNormalizedPath(route.normalisedPath)

                                    if (route.consumes.nonEmpty) customRoute.consumes(route.consumes.get.value)
                                    if (route.produces.nonEmpty) customRoute.produces(route.produces.get.value)
                                  }
                                }

                                // Common
                                //router.route(HttpMethod.POST, "/eventbus").handler(BodyHandler.create())
                                //router.route(HttpMethod.PUT, "/eventbus").handler(BodyHandler.create())
                                router.mountSubRouter("/eventbus", Handlers.sock(vx, eventBusInbound, eventBusOutbound))
                                router.get("/metrics").handler(PrometheusScrapingHandler.create())
                                router.get("/health").handler(rc => {
                                  val response = rc.response.putHeader("content-type", "text/plain")
                                  if (GCHealthCheck.current.isHealthy)
                                    response.setStatusCode(200).end("HEALTHY")
                                  else
                                    response.setStatusCode(503).end("UNHEALTHY")
                                })
                                router.get("/ready").handler(rc => rc.response.putHeader("content-type", "text/plain").setStatusCode(200).end("READY"))

                                // Public
                                // FIXME - Use StaticHandler in Production
                                router.get("/public/*").handler((rc: RoutingContext) => {
                                  val path = s"${System.getProperty("user.dir")}/src/main/resources${rc.request().uri}"
                                  sendFile(new File(path), vx, rc)
                                })

                                // CORS
                                router.route().handler(CorsHandler.create(".*.")
                                  .allowCredentials(true)
                                  .allowedHeaders((defaultAllowedHeaders ++ additionalAllowedHeaders).asJava)
                                  .allowedMethods(defaultAllowedMethods.asJava))

                                // Auth
                                if (authTypes.nonEmpty) {
                                  val clients = authTypes.map(AuthType.getClient(vx, publicUrl, _))
                                  val authConfig = new Pac4jConfig(new Clients(publicUrl + "/callback", clients: _*))
                                  authConfig.setHttpActionAdapter(new VertxHttpActionAdapter())

                                  val callbackHandlerOptions = new CallbackHandlerOptions().setDefaultUrl("/postLogin").setMultiProfile(true)
                                  val callbackHandler = new CallbackHandler(vx, sessionStore, authConfig, callbackHandlerOptions)

                                  if (sessionRegexp.nonEmpty) router.routeWithRegex(sessionRegexp.get).handler(sessionHandler)
                                  router.route.handler(sessionHandler)

                                  if (jwtKeySet.nonEmpty) router.get("/jwks").handler(Handlers.jwks(jwtKeySet.get))
                                  router.get("/callback").handler(callbackHandler)
                                  router.post("/callback").handler(BodyHandler.create().setMergeFormAttributes(true))
                                  router.post("/callback").handler(callbackHandler)
                                  router.get("/login").handler(Handlers.loginForm(vx, authConfig, "public/login.hbs", Map()))
                                  router.get("/forceLogin").handler(Handlers.forceLogin(authConfig, sessionStore))
                                  router.get("/confirm").handler(Handlers.loginForm(vx, authConfig, "public/login.hbs", Map()))
                                  router.get("/logout").handler(new LogoutHandler(vx, sessionStore, new LogoutHandlerOptions(), authConfig))
                                  router.get("/centralLogout").handler(Handlers.centralLogout(vx, authConfig, sessionStore, publicUrl))
                                  router.get("/postLogin").handler(rc => {
                                    val profileManager = new VertxProfileManager(new VertxWebContext(rc, sessionStore), sessionStore)
                                    val postLoginHandler = postLogin.get.apply(_, profileManager.getProfile.asScala)
                                    generateResponse(vx, logger, micrometer, templateEngine, uploadsDirectory, rc, RouteHandler.Standard(postLoginHandler))
                                  })
                                }

                                // Proxy
                                if (proxyDomain.nonEmpty && proxyMapping.nonEmpty) {
                                  val client = new WebProxyClient(webClient, WebProxyClientOptions(iFrameAncestors = List(domain, proxyDomain.get)))
                                  router.route().virtualHost(proxyDomain.get).blockingHandler(rc =>
                                    runEffect(proxyMapping.get(rc)) match {
                                      case Some(uri) => client.execute(rc, "/*", uri)
                                      case None => rc.response.end()
                                    }
                                  )
                                }

                                // Errors
                                router.route.failureHandler((rc: RoutingContext) => {
                                  val response = rc.response
                                  errorHandlers.get(response.getStatusCode) match {
                                    case Some(r) => generateResponse(vx, logger, micrometer, templateEngine, uploadsDirectory, rc, RouteHandler.Standard(r))
                                    case None => if (!response.closed() && !response.ended()) response.end()
                                  }
                                })

                                // Default handler
                                if (defaultHandler.nonEmpty)
                                  router.route.handler(rc => generateResponse(vx, logger, micrometer, templateEngine, uploadsDirectory, rc, defaultHandler.get))

                                router
                              }

    options                 <- ZIO.succeed {
                                var httpServerOptions = new HttpServerOptions()
                                  .setCompressionSupported(true)
                                  .setDecompressionSupported(true)
                                  .setLogActivity(logActivity)
                                  .setHandle100ContinueAutomatically(true)
                                  .setHost(listenHost)
                                  .setMaxHeaderSize(1024 * 16)
                                  .setPort(listenPort)
                                  .setSsl(useSSL)
                                  .setUseAlpn(getVersion >= 9)

                                  if (keyStorePath.nonEmpty) httpServerOptions = httpServerOptions.setKeyStoreOptions(
                                    new JksOptions().setPath(keyStorePath.get).setPassword(keyStorePassword.get)
                                  )

                                httpServerOptions
                              }

      httpServer            <- ZIO.async { (cb: Task[HttpServer] => Unit) =>
                                vx.createHttpServer(options)
                                  .requestHandler(router)
                                  .webSocketHandler(sourceSocket => {
                                    if (webSocketProxyMapping.nonEmpty && !sourceSocket.uri().startsWith("/eventbus")) {
                                      val target = runEffect(webSocketProxyMapping.get(sourceSocket.headers()))
                                      httpClient.webSocket(target.port, target.host, sourceSocket.uri(), (connection: AsyncResult[WebSocket]) => {
                                        if (connection.succeeded()) {
                                          val targetSocket = connection.result()
                                          syncSockets(sourceSocket, targetSocket)
                                        } else {
                                          logger.warn(s"Failed to connect to backend WS: $target")
                                        }
                                      })
                                    }
                                  })
                                  .listen(listenPort, listenHost, (result: AsyncResult[HttpServer]) =>
                                    if (result.succeeded())
                                      cb(
                                        (
                                          logger.info(s"Started HTTP server on $listenHost:$listenPort") *>
                                          logger.info(s"Routes: ${router.getRoutes.asScala.map(_.getPath).mkString(", ")}")
                                        ).as(result.result())
                                      )
                                    else
                                      cb(logger.error(s"Failed to start HTTP server on $listenHost:$listenPort") *> ZIO.fail(result.cause()))
                                  )
                                }

  } yield httpServer


  def startNetServer(listenHost: String, listenPort: Int, options: Option[NetServerOptions] = None): Task[NetServer] =
    ZIO.async { (cb: Task[NetServer] => Unit) =>
      vertxRef.get().createNetServer().listen(listenPort, listenHost, (result: AsyncResult[NetServer]) =>
        if (result.succeeded()) cb(ZIO.attempt(result.result())) else cb(ZIO.fail(result.cause())))
     }


  def startTcpEventBusServer(listenHost: String, listenPort: Int, inAddressRegex: String, outAddressRegex: String): Task[Unit] =
    ZIO.async { (cb: Task[Unit] => Unit) =>
        TcpEventBusBridge.create(vertxRef.get(), new BridgeOptions()
          .addInboundPermitted(new PermittedOptions().setAddressRegex(inAddressRegex))
          .addOutboundPermitted(new PermittedOptions().setAddressRegex(outAddressRegex)))
          .listen(listenPort, listenHost, (result: AsyncResult[TcpEventBusBridge]) =>
            if (result.succeeded()) cb(ZIO.succeed(result.result())) else cb(ZIO.fail(result.cause())))
      }
}