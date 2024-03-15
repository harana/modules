package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.connect.FeeRefund
import zio.{IO, ZIO, ZLayer}

object LiveStripeApplicationFeeRefunds {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeApplicationFeeRefunds(config, logger, micrometer)
  }
}

case class LiveStripeApplicationFeeRefunds(config: Config, logger: Logger, micrometer: Micrometer) extends StripeApplicationFeeRefunds {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).applicationFees.refunds)

  def create(feeId: String,
             amount: Option[Money] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, FeeRefund] =
    for {
      c <- client
      r <- execute(c.create(feeId, amount, metadata))
    } yield r


  def byId(feeId: String, refundId: String): IO[ResponseError, FeeRefund] =
    for {
      c <- client
      r <- execute(c.byId(feeId, refundId))
    } yield r


  def update(feeId: String, refundId: String, metadata: Map[String, String] = Map.empty): IO[ResponseError, FeeRefund] =
    for {
      c <- client
      r <- execute(c.update(feeId, refundId, metadata))
    } yield r


  def list(feeId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[FeeRefund]] =
    for {
      c <- client
      r <- execute(c.list(feeId, config))
    } yield r
}