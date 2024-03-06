package com.harana.modules.vertx.models

import io.vertx.core.{AbstractVerticle, Promise}
import zio._

// FIXME
//trait Verticle extends AbstractVerticle {
//
//  def run: ZIO[Nothing, Nothing, Int]
//
//  override def start(startPromise: Promise[Void]): Unit = {
//    Unsafe.unsafe { implicit unsafe =>
//      Runtime.default.unsafe.run(
//        (for {
//          fiber <- run.fork
//          _ <- ZIO.succeed(java.lang.Runtime.getRuntime.addShutdownHook(new Thread {
//            override def run() = {
//              val _ = Runtime.default.unsafe.run(fiber.interrupt)
//            }
//          }))
//          result <- fiber.join
//          _ <- fiber.interrupt
//        } yield result)
//      )
//    }
//  }
//}