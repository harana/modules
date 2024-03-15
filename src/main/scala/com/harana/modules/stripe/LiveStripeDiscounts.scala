package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe.{Deleted, ResponseError, Stripe}
import zio.{IO, ZIO, ZLayer}

object LiveStripeDiscounts {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeDiscounts(config, logger, micrometer)
  }
}

case class LiveStripeDiscounts(config: Config, logger: Logger, micrometer: Micrometer) extends StripeDiscounts {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).discounts)

  def deleteCustomerDiscount(customerId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.deleteCustomerDiscount(customerId))
    } yield r


  def deleteSubscriptionDiscount(subscriptionId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.deleteSubscriptionDiscount(subscriptionId))
    } yield r

}