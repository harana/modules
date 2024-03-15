package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.refund.Refund
import zio.{IO, ZIO, ZLayer}

object LiveStripeRefunds {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeRefunds(config, logger, micrometer)
  }
}

case class LiveStripeRefunds(config: Config, logger: Logger, micrometer: Micrometer) extends StripeRefunds {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).refunds)

  def create(chargeId: String,
             amount: Option[Money] = None,
             metadata: Map[String, String] = Map.empty,
             reason: Option[String] = None,
             refundApplicationFee: Boolean = false,
             reverseTransfer: Boolean = false): IO[ResponseError, Refund] =
    for {
      c <- client
      r <- execute(c.create(chargeId, amount, metadata, reason, refundApplicationFee, reverseTransfer))
    } yield r


  def byId(refundId: String): IO[ResponseError, Refund] =
    for {
      c <- client
      r <- execute(c.byId(refundId))
    } yield r


  def update(refundId: String, metadata: Map[String, String] = Map.empty): IO[ResponseError, Refund] =
    for {
      c <- client
      r <- execute(c.update(refundId, metadata))
    } yield r


  def list(chargeId: Option[String] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Refund]] =
    for {
      c <- client
      r <- execute(c.list(chargeId, config))
    } yield r

}