package com.harana.modules.thumbnailator

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.thumbnailator.streams.ByteBufferInputStream
import com.harana.modules.vertx.models.streams.VertxBufferOutputStream
import io.vertx.core.buffer.Buffer
import net.coobird.thumbnailator.Thumbnails
import zio.{Task, ZIO, ZLayer}

import java.nio.ByteBuffer
import scala.jdk.CollectionConverters._

object LiveThumbnailator {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveThumbnailator(config, logger, micrometer)
  }
}

case class LiveThumbnailator(config: Config, logger: Logger, micrometer: Micrometer) extends Thumbnailator {

    def thumbnailAsVertxBuffer(byteBuffer: ByteBuffer,
                               width: Option[Int] = None,
                               height: Option[Int] = None,
                               keepAspectRatio: Boolean = true,
                               outputFormat: String = "JPEG"): Task[Buffer] =
     ZIO.attempt {
       val builder = Thumbnails.fromInputStreams(Seq(new ByteBufferInputStream(byteBuffer)).asJava)
       val os = new VertxBufferOutputStream

       if (width.nonEmpty) builder.width(width.get)
       if (height.nonEmpty) builder.height(height.get)
       builder.keepAspectRatio(keepAspectRatio)
       builder.outputFormat(outputFormat)
       builder.toOutputStream(os)
       os.buffer
     }

}