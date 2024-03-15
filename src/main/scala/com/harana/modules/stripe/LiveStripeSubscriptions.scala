package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.subscription.{CreateSubscriptionItem, Subscription}
import zio.{IO, ZIO, ZLayer}

object LiveStripeSubscriptions {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeSubscriptions(config, logger, micrometer)
  }
}

case class LiveStripeSubscriptions(config: Config, logger: Logger, micrometer: Micrometer) extends StripeSubscriptions {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).subscriptions)

  def create(customerId: String,
             items: List[CreateSubscriptionItem],
             applicationFeePercent: Option[BigDecimal] = None,
             coupon: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             prorate: Option[Boolean] = None,
             quantity: Option[Int] = None,
             source: Option[String] = None,
             taxPercent: Option[BigDecimal] = None,
             trialEnd: Option[Long] = None,
             trialPeriodDays: Option[Int] = None): IO[ResponseError, Subscription] =
    for {
      c <- client
      r <- execute(c.create(customerId, items, applicationFeePercent, coupon, metadata, prorate, quantity, source, taxPercent, trialEnd, trialPeriodDays))
    } yield r


  def byId(subscriptionId: String): IO[ResponseError, Subscription] =
    for {
      c <- client
      r <- execute(c.byId(subscriptionId))
    } yield r


  def update(subscriptionId: String,
             items: List[CreateSubscriptionItem] = List(),
             applicationFeePercent: Option[BigDecimal] = None,
             coupon: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             prorate: Option[Boolean] = None,
             prorationDate: Option[Long] = None,
             quantity: Option[Int] = None,
             source: Option[String] = None,
             taxPercent: Option[BigDecimal],
             trialEnd: Option[Long] = None,
             trialPeriodDays: Option[Int] = None): IO[ResponseError, Subscription] =
    for {
      c <- client
      r <- execute(c.update(subscriptionId, items, applicationFeePercent, coupon, metadata, prorate, prorationDate, quantity, source, taxPercent, trialEnd, trialPeriodDays))
    } yield r


  def cancel(customerId: String,
             subscriptionId: String,
             atPeriodEnd: Boolean = false): IO[ResponseError, Subscription] =
    for {
      c <- client
      r <- execute(c.cancel(customerId, subscriptionId, atPeriodEnd))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           customer: Option[String] = None,
           plan: Option[String] = None,
           status: Option[String] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Subscription]] =
    for {
      c <- client
      r <- execute(c.list(created, customer, plan, status, config))
    } yield r

}