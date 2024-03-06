package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.connect.ApplicationFee
import zio.{IO, ZIO, ZLayer}

object LiveStripeApplicationFees {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeApplicationFees(config, logger, micrometer)
  }
}

case class LiveStripeApplicationFees(config: Config, logger: Logger, micrometer: Micrometer) extends StripeApplicationFees {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).applicationFees)

  def byId(feeId: String): IO[ResponseError, ApplicationFee] =
    for {
      c <- client
      r <- execute(c.byId(feeId))
    } yield r


  def list(charge: Option[String] = None,
           created: Option[TimestampFilter] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[ApplicationFee]] =
    for {
      c <- client
      r <- execute(c.list(charge, created, config))
    } yield r

}