package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.subscription.{Invoice, InvoiceLine}
import zio.{IO, ZIO, ZLayer}

object LiveStripeInvoices {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeInvoices(config, logger, micrometer)
  }
}

case class LiveStripeInvoices(config: Config, logger: Logger, micrometer: Micrometer) extends StripeInvoices {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).invoices)

  def create(customerId: String,
             applicationFee: Option[Money] = None,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             statementDescriptor: Option[String] = None,
             subscription: Option[String] = None,
             taxPercent: Option[BigDecimal] = None): IO[ResponseError, Invoice] =
    for {
      c <- client
      r <- execute(c.create(customerId, applicationFee, description, metadata, statementDescriptor, subscription, taxPercent))
    } yield r


  def byId(invoiceId: String): IO[ResponseError, Invoice] =
    for {
      c <- client
      r <- execute(c.byId(invoiceId))
    } yield r


  def linesById(invoiceId: String,
                coupon: Option[String] = None,
                customer: Option[String] = None,
                subscription: Option[String] = None,
                subscriptionPlan: Option[String] = None,
                subscriptionProrate: Option[String] = None,
                subscriptionProrationDate: Option[Long] = None,
                subscriptionQuantity: Option[Int] = None,
                subscriptionTrialEnd: Option[Long] = None,
                config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[InvoiceLine]] =
    for {
      c <- client
      r <- execute(c.linesById(invoiceId, coupon, customer, subscription, subscriptionPlan, subscriptionProrate, subscriptionProrationDate, subscriptionQuantity,
        subscriptionTrialEnd, config))
    } yield r


  def upcoming(customerId: String,
               coupon: Option[String] = None,
               subscription: Option[String] = None,
               subscriptionPlan: Option[String] = None,
               subscriptionProrate: Option[String] = None,
               subscriptionProrationDate: Option[Long] = None,
               subscriptionQuantity: Option[Int] = None,
               subscriptionTrialEnd: Option[Long] = None): IO[ResponseError, Invoice] =
    for {
      c <- client
      r <- execute(c.upcoming(customerId, coupon, subscription, subscriptionPlan, subscriptionProrate, subscriptionProrationDate, subscriptionQuantity, subscriptionTrialEnd))
    } yield r


  def update(invoiceId: String,
             applicationFee: Option[Money] = None,
             closed: Option[Boolean] = None,
             description: Option[String] = None,
             forgiven: Option[Boolean] = None,
             metadata: Map[String, String] = Map.empty,
             statementDescriptor: Option[String] = None,
             taxPercent: Option[BigDecimal] = None): IO[ResponseError, Invoice] =
    for {
      c <- client
      r <- execute(c.update(invoiceId, applicationFee, closed, description, forgiven, metadata, statementDescriptor, taxPercent))
    } yield r


  def pay(invoiceId: String): IO[ResponseError, Invoice] =
    for {
      c <- client
      r <- execute(c.pay(invoiceId))
    } yield r


  def list(customerId: Option[String] = None,
           date: Option[TimestampFilter] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Invoice]] =
    for {
      c <- client
      r <- execute(c.list(customerId, date, config))
    } yield r

}