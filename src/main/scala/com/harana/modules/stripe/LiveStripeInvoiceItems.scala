package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.subscription.InvoiceItem
import zio.{IO, ZIO, ZLayer}

object LiveStripeInvoiceItems {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeInvoiceItems(config, logger, micrometer)
  }
}

case class LiveStripeInvoiceItems(config: Config, logger: Logger, micrometer: Micrometer) extends StripeInvoiceItems {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).invoiceItems)

  def create(amount: Money,
             currency: String,
             customerId: String,
             description: Option[String] = None,
             discountable: Option[Boolean] = None,
             invoice: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             subscription: Option[String] = None): IO[ResponseError, InvoiceItem] =
    for {
      c <- client
      r <- execute(c.create(amount, currency, customerId, description, discountable, invoice, metadata, subscription))
    } yield r


  def byId(invoiceItemId: String): IO[ResponseError, InvoiceItem] =
    for {
      c <- client
      r <- execute(c.byId(invoiceItemId))
    } yield r


  def update(invoiceItemId: String,
             amount: Option[Money] = None,
             description: Option[String] = None,
             discountable: Option[Boolean] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, InvoiceItem] =
    for {
      c <- client
      r <- execute(c.update(invoiceItemId, amount, description, discountable, metadata))
    } yield r


  def delete(invoiceItemId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(invoiceItemId))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           customer: Option[String] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[InvoiceItem]] =
    for {
      c <- client
      r <- execute(c.list(created, customer, config))
    } yield r

}