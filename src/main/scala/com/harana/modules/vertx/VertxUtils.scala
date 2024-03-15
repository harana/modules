package com.harana.modules.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.streams.Pump
import io.vertx.ext.reactivestreams.ReactiveWriteStream
import io.vertx.ext.web.RoutingContext
import org.reactivestreams.{Subscriber, Subscription}
import zio._

import scala.collection.mutable.ArrayBuffer

object VertxUtils {

    def streamToString(rc: RoutingContext, stream: ReactiveWriteStream[Buffer], streamPump: Pump) =
    ZIO.async((cb: Task[String] => Unit) => stream.subscribe(new Subscriber[Buffer] {
      val bytes = ArrayBuffer.empty[Byte]
      var remaining = rc.request().getHeader(HttpHeaders.CONTENT_LENGTH).toLong
      println("Waiting for subscription")

      def onSubscribe(s: Subscription) = {
        println("Subscribed to stream .. starting pump")
        streamPump.start()
        s.request(remaining)
      }
      def onNext(t: Buffer) = {
        bytes.addAll(t.getBytes)
        remaining -= t.length()
        if (remaining == 0) onComplete()
      }
      def onError(t: Throwable) = cb(ZIO.succeed(streamPump.stop()) *> ZIO.fail(t))
      def onComplete() = cb({
        println("Completed stream .. ")
        ZIO.succeed(streamPump.stop()) *> ZIO.attempt(new String(bytes.toArray))
      })
    }))
}
