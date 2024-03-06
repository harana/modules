package com.harana.modules.thumbnailator

import io.vertx.core.buffer.Buffer
import zio.Task
import zio.macros.accessible

import java.nio.ByteBuffer

@accessible
trait Thumbnailator {

    def thumbnailAsVertxBuffer(byteBuffer: ByteBuffer,
                               width: Option[Int] = None,
                               height: Option[Int] = None,
                               keepAspectRatio: Boolean = true,
                               outputFormat: String = "JPEG"): Task[Buffer]
}