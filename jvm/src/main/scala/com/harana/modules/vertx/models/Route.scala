package com.harana.modules.vertx.models

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.streams.Pump
import io.vertx.ext.reactivestreams.ReactiveWriteStream
import io.vertx.ext.web.{RoutingContext, FileUpload => VertxFileUpload}
import zio.Task

import java.nio.file.Path

case class Route(path: String,
                 method: HttpMethod,
                 handler: RouteHandler,
                 consumes: Option[ContentType] = None,
                 produces: Option[ContentType] = Some(ContentType.HTML),
                 multipart: Boolean = false,
                 secured: Boolean = false,
                 regex: Boolean = false,
                 normalisedPath: Boolean = true,
                 blocking: Boolean = false)

sealed trait RouteHandler
object RouteHandler {
  case class Standard(handler: RoutingContext => Task[Response]) extends RouteHandler
  case class FileUpload(handler: (RoutingContext, Path, List[VertxFileUpload]) => Task[Response]) extends RouteHandler
  case class Stream(handler: (RoutingContext, ReactiveWriteStream[Buffer], Pump) => Task[Response]) extends RouteHandler
}