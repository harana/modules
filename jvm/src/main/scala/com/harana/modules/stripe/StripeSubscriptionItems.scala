package com.harana.modules.stripe

import com.outr.stripe.subscription.SubscriptionItem
import com.outr.stripe.{Deleted, QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeSubscriptionItems {
   def create(subscriptionId: String,
               billingThresholds: Map[String, String] = Map(),
               metadata: Map[String, String] = Map(),
               paymentBehavior: Option[String] = None,
               priceId: Option[String] = None,
               prorationBehavior: Option[String] = None,
               prorationDate: Option[Long] = None,
               quantity: Option[Int] = None,
               taxRates: List[String] = List()): IO[ResponseError, SubscriptionItem]

    def byId(subscriptionItemId: String): IO[ResponseError, SubscriptionItem]

    def update(subscriptionItemId: String,
               billingThresholds: Map[String, String] = Map(),
               metadata: Map[String, String] = Map(),
               offSession: Option[Boolean] = None,
               paymentBehavior: Option[String] = None,
               priceId: Option[String] = None,
               prorationBehavior: Option[String] = None,
               prorationDate: Option[Long] = None,
               quantity: Option[Int] = None,
               taxRates: List[String] = List()): IO[ResponseError, SubscriptionItem]

    def delete(subscriptionItemId: String): IO[ResponseError, Deleted]

    def list(subscription: String,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[SubscriptionItem]]
}