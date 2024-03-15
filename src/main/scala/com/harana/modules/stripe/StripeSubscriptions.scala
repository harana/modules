package com.harana.modules.stripe

import com.outr.stripe.subscription.{CreateSubscriptionItem, Subscription}
import com.outr.stripe.{QueryConfig, ResponseError, StripeList, TimestampFilter}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeSubscriptions {
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
               trialPeriodDays: Option[Int] = None): IO[ResponseError, Subscription]

    def byId(subscriptionId: String): IO[ResponseError, Subscription]

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
               trialPeriodDays: Option[Int] = None): IO[ResponseError, Subscription]

    def cancel(customerId: String,
               subscriptionId: String,
               atPeriodEnd: Boolean = false): IO[ResponseError, Subscription]

    def list(created: Option[TimestampFilter] = None,
             customer: Option[String] = None,
             plan: Option[String] = None,
             status: Option[String] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Subscription]]
}