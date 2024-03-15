package com.harana.modules.vertx.models

import io.circe.Json
import io.vertx.core.buffer.{Buffer => VertxBuffer}
import io.vertx.core.http.Cookie
import io.vertx.core.streams.{ReadStream => VertxReadStream}

sealed trait Response {
  val contentType: Option[ContentType]
  val cookies: List[Cookie]
  val statusCode: Option[Int]
  val headers: Map[_<: CharSequence, List[_<: CharSequence]]
}

object Response {

  case class Buffer(buffer: VertxBuffer,
                    gzipped: Boolean = false,
                    contentType: Option[ContentType] = None,
                    cookies: List[Cookie] = List(),
                    statusCode: Option[Int] = None,
                    cors: Boolean = false,
                    headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class Content(content: String,
                     contentType: Option[ContentType] = None,
                     cookies: List[Cookie] = List(),
                     statusCode: Option[Int] = None,
                     cors: Boolean = false,
                     headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class Empty(contentType: Option[ContentType] = None,
                   cookies: List[Cookie] = List(),
                   statusCode: Option[Int] = None,
                   cors: Boolean = false,
                   headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class File(filename: String,
                  inputStream: java.io.InputStream,
                  gzipped: Boolean = false,
                  contentSize: Option[Long] = None,
                  contentType: Option[ContentType] = None,
                  cookies: List[Cookie] = List(),
                  statusCode: Option[Int] = None,
                  cors: Boolean = false,
                  headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class InputStream(inputStream: java.io.InputStream,
                         gzipped: Boolean = false,
                         contentSize: Option[Long] = None,
                         contentType: Option[ContentType] = None,
                         cookies: List[Cookie] = List(),
                         statusCode: Option[Int] = None,
                         cors: Boolean = false,
                         headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class JSON(content: Json,
                  contentType: Option[ContentType] = Some(ContentType.JSON),
                  cookies: List[Cookie] = List(),
                  statusCode: Option[Int] = None,
                  cors: Boolean = false,
                  headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class ReadStream(stream: VertxReadStream[VertxBuffer],
                        contentSize: Option[Long] = None,
                        contentType: Option[ContentType] = None,
                        cookies: List[Cookie] = List(),
                        statusCode: Option[Int] = None,
                        cors: Boolean = false,
                        headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class Redirect(url: String,
                      contentType: Option[ContentType] = None,
                      cookies: List[Cookie] = List(),
                      statusCode: Option[Int] = None,
                      cors: Boolean = false,
                      headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response

  case class Template(path: String,
                      parameters: Map[String, AnyRef] = Map(),
                      contentType: Option[ContentType] = None,
                      cookies: List[Cookie] = List(),
                      statusCode: Option[Int] = None,
                      cors: Boolean = false,
                      headers: Map[_<: CharSequence, List[_<: CharSequence]] = Map()) extends Response
}