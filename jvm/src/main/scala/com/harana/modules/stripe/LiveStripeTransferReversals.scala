package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.transfer.TransferReversal
import zio.{IO, ZIO, ZLayer}

object LiveStripeTransferReversals {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeTransferReversals(config, logger, micrometer)
  }
}

case class LiveStripeTransferReversals(config: Config, logger: Logger, micrometer: Micrometer) extends StripeTransferReversals {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).transfers.reversals)

  def create(transferId: String,
             amount: Option[Money] = None,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             refundApplicationFee: Boolean = false): IO[ResponseError, TransferReversal] =
    for {
      c <- client
      r <- execute(c.create(transferId, amount, description, metadata, refundApplicationFee))
    } yield r


  def byId(transferId: String, transferReversalId: String): IO[ResponseError, TransferReversal] =
    for {
      c <- client
      r <- execute(c.byId(transferId, transferReversalId))
    } yield r


  def update(transferId: String,
             transferReversalId: String,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, TransferReversal] =
    for {
      c <- client
      r <- execute(c.update(transferId, transferReversalId, description, metadata))
    } yield r


  def list(transferId: String,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[TransferReversal]] =
    for {
      c <- client
      r <- execute(c.list(transferId, config))
    } yield r

}