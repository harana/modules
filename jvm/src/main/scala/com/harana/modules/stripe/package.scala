package com.harana.modules

import zio.{IO, ZIO}

import scala.concurrent.Future

package object stripe {

  def execute[E, A](output: Future[Either[E, A]]): IO[E, A] =
    ZIO.succeed(output).flatMap { o =>
      ZIO.fromFuture { _ =>
        o
      }.orDie.absolve
    }
}