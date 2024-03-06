package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.transfer.Transfer
import zio.{IO, ZIO, ZLayer}

object LiveStripeTransfers {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeTransfers(config, logger, micrometer)
  }
}

case class LiveStripeTransfers(config: Config, logger: Logger, micrometer: Micrometer) extends StripeTransfers {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).transfers)

  def create(amount: Money,
             currency: String,
             destination: String,
             applicationFee: Option[Money] = None,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             sourceTransaction: Option[String] = None,
             statementDescriptor: Option[String] = None,
             sourceType: String = "card",
             method: String = "standard"): IO[ResponseError, Transfer] =
    for {
      c <- client
      r <- execute(c.create(amount, currency, destination, applicationFee, description, metadata, sourceTransaction, statementDescriptor, sourceType, method))
    } yield r


  def byId(transferId: String): IO[ResponseError, Transfer] =
    for {
      c <- client
      r <- execute(c.byId(transferId))
    } yield r


  def update(transferId: String,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, Transfer] =
    for {
      c <- client
      r <- execute(c.update(transferId, description, metadata))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           date: Option[TimestampFilter] = None,
           destination: Option[String] = None,
           recipient: Option[String] = None,
           status: Option[String] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Transfer]] =
    for {
      c <- client
      r <- execute(c.list(created, date, destination, recipient, status, config))
    } yield r

}