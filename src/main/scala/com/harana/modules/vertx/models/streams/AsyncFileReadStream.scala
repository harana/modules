package com.harana.modules.vertx.models.streams

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import one.jasyncfio.{AsyncFile, EventExecutor}
import org.apache.commons.lang3.SystemUtils

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}
import java.nio.file.{Paths, StandardOpenOption}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.util.Try

class AsyncFileReadStream(path: String, range: Option[(Long, Long)] = None) extends ReadStream[Buffer] {

  val file =
    if (SystemUtils.IS_OS_LINUX)
      Left(AsyncFile.open(path, EventExecutor.initDefault()).get())
    else
      Right(AsynchronousFileChannel.open(Paths.get(path), StandardOpenOption.READ))

  var closed = false
  var readPos = if (range.nonEmpty) range.get._1 else 0L
  val readBufferSize = 1024
  var readLength = if (range.nonEmpty) (range.get._2 - range.get._1)+1 else Long.MaxValue

  var handler: Option[Handler[Buffer]] = None
  var exceptionHandler: Option[Handler[Throwable]] = None
  var endHandler: Option[Handler[Void]] = None

  var queue = new InboundBuffer[Buffer](0)
  queue.drainHandler(_ => doRead(ByteBuffer.allocateDirect(readBufferSize)))

  queue.handler { buff =>
    if (buff.length() > 0) {
      if (this.handler.nonEmpty) this.handler.get.handle(buff)
    } else {
      if (this.endHandler.nonEmpty) this.endHandler.get.handle(null)
    }
  }

  def doRead(bb: ByteBuffer): Unit = {
    val buff = Buffer.buffer(readBufferSize)
    val readSize = Math.min(readBufferSize, readLength).toInt
    bb.limit(readSize)
    val promise = Promise[Buffer]()
    promise.future.onComplete { (ar: Try[Buffer]) => {
      if (ar.isSuccess) {
        val buffer = ar.get
        readPos += buffer.length()
        readLength -= buffer.length()
        if (buffer.length == 0) {
          if (this.endHandler.nonEmpty) {
            this.endHandler.get.handle(null)
          }
        } else
            if (queue.write(buffer)) doRead(bb)
      } else
        if (this.exceptionHandler.nonEmpty) this.exceptionHandler.get.handle(ar.failed.get)
    }}

    read(buff, 0, bb, readPos, promise)
  }

  def read(writeBuff: Buffer, offset: Int, buff: ByteBuffer, position: Long, promise: Promise[Buffer]): Unit =
    file match {
      case Left(asyncFile) =>
        val tempBuffer = ByteBuffer.allocateDirect(readBufferSize)
        var read = position

        while (read != 1 && buff.hasRemaining) {
          read = asyncFile.read(tempBuffer, read, readBufferSize).get().toLong
          promise.success(Buffer.buffer(tempBuffer.array()))
        }

      case Right(channel) =>
        channel.read(buff, position, null, new CompletionHandler[Integer, Object]() {
          var pos = position

          def completed(bytesRead: Integer, attachment: Object) =
            if (bytesRead == -1)
              done()
            else {
              if (buff.hasRemaining) {
                pos += bytesRead
                read(writeBuff, offset, buff, pos, promise)
              } else
                done()
            }

          def failed(t: Throwable, attachment: Object) = {
            t.printStackTrace()
            promise.failure(t)
          }

          def done() = {
            buff.flip()
            writeBuff.setBytes(offset, buff)
            buff.compact()
            promise.success(writeBuff)
          }
        })
    }

  def handler(handler: Handler[Buffer]) = {
    if (closed)
      this
    else {
      this.handler = Option(handler)
      if (this.handler.nonEmpty)
        doRead(ByteBuffer.allocateDirect(readBufferSize))
      else
        queue.clear()
    }
    this
  }

  def pause() = {
    queue.pause()
    this
  }

  def resume() = {
    if (!closed) queue.resume()
    this
  }

  def fetch(amount: Long) = {
    queue.fetch(amount)
    this
  }

  def exceptionHandler(handler: Handler[Throwable]) = {
    this.exceptionHandler = Option(handler)
    this
  }

  def endHandler(handler: Handler[Void]) = {
    this.endHandler = Some(handler)
    this
  }
}