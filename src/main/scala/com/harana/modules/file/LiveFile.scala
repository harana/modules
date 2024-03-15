package com.harana.modules.file

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.file.LiveFile.{chunk_size, eventExecutor}
import com.harana.modules.vertx.models.streams.AsyncFileReadStream
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, jawn}
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.ext.reactivestreams.ReactiveWriteStream
import one.jasyncfio.{AsyncFile, EventExecutor}
import org.apache.commons.lang3.SystemUtils
import org.reactivestreams.{Subscriber, Subscription}
import zio.{Task, ZIO, ZLayer}

import java.io.{FileInputStream, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util

object LiveFile {
  val chunk_size = 1024
  val eventExecutor = if (SystemUtils.IS_OS_LINUX) Some(EventExecutor.initDefault()) else None

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveFile(config, logger, micrometer)
  }
}

case class LiveFile(config: Config, logger: Logger, micrometer: Micrometer) extends File {

  def readStream(path: Path, range: Option[(Long, Long)] = None): Task[ReadStream[Buffer]] =
    ZIO.attempt(new AsyncFileReadStream(path.toFile.getAbsolutePath, range))


  def read(file: Either[Path, AsyncFile], buffer: ByteBuffer, position: Option[Int] = None) = {
    file match {
      case Left(path) =>
        ZIO.attempt {
          val bytes = Files.readAllBytes(path)
          buffer.put(bytes)
          bytes.size
        }

      case Right(file) =>
        ZIO.fromFutureJava(if (position.nonEmpty) file.read(buffer, position.get) else file.read(buffer)).map(_.toInt)
    }
  }


  def readJson[A](path: Path)(implicit decoder: Decoder[A]): Task[A] =
    ZIO.fromEither(jawn.decode[A](Files.readString(path)))


  def readString(path: Path): Task[String] =
    ZIO.attempt(Files.readString(path))


  def writeAwsStream(path: Path,
                     stream: ReactiveWriteStream[Buffer],
                     length: Long,
                     onStart: Option[() => Any] = None,
                     onStop: Option[() => Any] = None): Task[Unit] = {
    var emptyChunk = false
    var headerEndPos = -1
    var chunkEndPos = -1
    val crlf = "\r\n".getBytes(StandardCharsets.UTF_8)
    val delimiter = ";".getBytes(StandardCharsets.UTF_8)
    val buffer = Buffer.buffer()

    def countUntil(data: Buffer, sequence: Array[Byte], start: Int): Int = {
      for (i <- start to data.length()) {
        if (i + sequence.length < data.length()) {
          val bytes = data.getBytes(i, i + sequence.length)
          if (util.Arrays.equals(bytes, sequence)) return i
        }
      }
      -1
    }

    writeStream(path, stream, length, onStart, onStop, Some(data => {
        buffer.appendBuffer(data)

        if (headerEndPos == -1) {
          val delimiterPos = countUntil(buffer, delimiter, 0) + delimiter.length
          emptyChunk = buffer.slice(0,2).toString(StandardCharsets.UTF_8).equals("0;")
          headerEndPos = countUntil(buffer, crlf, delimiterPos) + crlf.length
        }

        if (headerEndPos > 0 && chunkEndPos == -1) {
          chunkEndPos = countUntil(buffer, crlf, headerEndPos)
        }

        if (headerEndPos > 0 && chunkEndPos > 0) {
          (buffer.slice(headerEndPos + 1, chunkEndPos), true)
        } else {
          (Buffer.buffer(), emptyChunk)
        }
      })
    )
  }


  def writeStream(path: Path,
                  stream: ReactiveWriteStream[Buffer],
                  length: Long,
                  onStart: Option[() => Any] = None,
                  onStop: Option[() => Any] = None,
                  onData: Option[Buffer => (Buffer, Boolean)] = None): Task[Unit] =
    if (eventExecutor.nonEmpty)
      for {
        file    <- ZIO.fromCompletableFuture(AsyncFile.open(path, eventExecutor.get))
        _       <- ZIO.async((cb: Task[Unit] => Unit) =>
                    stream.subscribe(new Subscriber[Buffer] {
                      var subscription: Subscription = _
                      var remaining = length

                      override def onSubscribe(sub: Subscription) = {
                        subscription = sub
                        if (onStart.nonEmpty) onStart.get.apply()
                        sub.request(if (remaining > chunk_size) chunk_size else remaining)
                      }

                      override def onNext(t: Buffer) = {
                        val onDataResult = onData.map(_.apply(t))
                        val data = if (onDataResult.nonEmpty) onDataResult.get._1 else t
                        file.write(data.getByteBuf.nioBuffer())

                        remaining -= data.length()
                        if (remaining == 0 || (onDataResult.nonEmpty && onDataResult.get._2)) {
                          subscription.cancel()
                          onComplete()
                        } else {
                          subscription.request(if (remaining > chunk_size) chunk_size else remaining)
                        }
                      }

                      override def onError(t: Throwable) = throw t

                      override def onComplete() = {
                        file.close()
                        if (onStop.nonEmpty) onStop.get.apply()
                        cb(ZIO.unit)
                      }
                    })
                )
      } yield ()
    else
      ZIO.async { (cb: Task[Unit] => Unit) =>
        stream.subscribe(new Subscriber[Buffer] {
          var subscription: Subscription = _
          var remaining = length
          var fos: FileOutputStream = _

          override def onSubscribe(sub: Subscription) = {
            subscription = sub
            fos = new FileOutputStream(path.toFile)
            if (onStart.nonEmpty) onStart.get.apply()
            subscription.request(if (remaining > chunk_size) chunk_size else remaining)
          }

          override def onNext(t: Buffer) = {
            val onDataResult = onData.map(_.apply(t))
            val data = if (onDataResult.nonEmpty) onDataResult.get._1 else t
            fos.write(data.getBytes)
            remaining -= data.length()

            if (remaining <= 0 || (onDataResult.nonEmpty && onDataResult.get._2)) {
              subscription.cancel()
              onComplete()
            } else
              subscription.request(if (remaining > chunk_size) chunk_size else remaining)
          }

          override def onError(t: Throwable) = cb(ZIO.fail(t))

          override def onComplete() = {
            fos.close()
            if (onStop.nonEmpty) onStop.get.apply()
            cb(ZIO.unit)
          }
        })
      }


  def write(file: Either[Path, AsyncFile], buffer: ByteBuffer, position: Option[Int] = None) =
    file match {
      case Left(path) =>
        ZIO.attempt {
          val array = buffer.array()
          Files.write(path, array)
          array.size
        }

      case Right(file) =>
        ZIO.fromFutureJava(if (position.nonEmpty) file.write(buffer, position.get) else file.write(buffer)).map(_.toInt)
    }


  def writeJson[A](path: Path, obj: A)(implicit encoder: Encoder[A]): Task[Unit] = {
    ZIO.attempt(Files.writeString(path, obj.asJson.noSpaces, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.SYNC))
  }


  def writeString(path: Path, string: String): Task[Unit] = {
    ZIO.attempt(Files.writeString(path, string, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.SYNC))
  }


  def merge(sourcePaths: List[Path], targetPath: Path): Task[Unit] =
    ZIO.attempt {
      val target = new FileOutputStream(targetPath.toFile, true).getChannel
      sourcePaths.foreach { path =>
        val fis = new FileInputStream(path.toFile).getChannel
        fis.transferFrom(target, Files.size(path), target.size())
        fis.close()
      }
    }


  def close(file: Either[Path, AsyncFile]) =
    file match {
      case Left(path) =>
        ZIO.unit
      case Right(file) =>
        ZIO.fromFutureJava(file.close()).map(_.toInt)
    }
}