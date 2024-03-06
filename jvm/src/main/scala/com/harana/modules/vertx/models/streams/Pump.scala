package com.harana.modules.vertx.models.streams

import io.vertx.core.streams.impl.PumpImpl
import io.vertx.core.streams.{ReadStream, WriteStream}

case class Pump[T](rs: ReadStream[T], ws: WriteStream[T]) extends PumpImpl(rs, ws) {

  override def start() = {
    rs.resume()
    super.start()
  }

}