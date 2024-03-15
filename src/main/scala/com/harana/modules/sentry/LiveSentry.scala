package com.harana.modules.sentry

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import io.sentry.{Breadcrumb, Sentry => ioSentry}
import zio.{UIO, ZIO, ZLayer}

object LiveSentry {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveSentry(config, logger, micrometer)
  }
}

case class LiveSentry(config: Config, logger: Logger, micrometer: Micrometer) extends Sentry {

  // FIXME
  config.string("sentry.dsn", "").map(ioSentry.init)

  def addBreadcrumb(message: String): UIO[Unit] = {
    ZIO.succeed(ioSentry.addBreadcrumb(message))
  }

  def addBreadcrumb(breadcrumb: Breadcrumb): UIO[Unit] = {
    ZIO.succeed(ioSentry.addBreadcrumb(breadcrumb))
  }

}