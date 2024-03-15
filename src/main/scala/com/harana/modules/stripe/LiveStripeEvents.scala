package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.event.Event
import zio.{IO, ZIO, ZLayer}

object LiveStripeEvents {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeEvents(config, logger, micrometer)
  }
}

case class LiveStripeEvents(config: Config, logger: Logger, micrometer: Micrometer) extends StripeEvents {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).events)

  def byId(eventId: String): IO[ResponseError, Event] =
    for {
      c <- client
      r <- execute(c.byId(eventId))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           `type`: Option[String] = None,
           types: List[String] = Nil,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Event]] =
    for {
      c <- client
      r <- execute(c.list(created, `type`, types, config))
    } yield r

}
