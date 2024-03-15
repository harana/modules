package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.price.{Price, Recurring, Tier, TransformQuantity}
import zio.IO
import zio.macros.accessible

@accessible
trait StripePrices {
   def create(currency: String,
               active: Option[Boolean] = None,
               billingScheme: Option[String] = None,
               lookupKey: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               nickname: Option[String] = None,
               recurring: Option[Recurring] = None,
               tiers: List[Tier] = List(),
               tiersMode: Option[String] = None,
               transferLookupKey: Option[Boolean] = None,
               transformQuantity: Option[TransformQuantity] = None,
               unitAmount: Option[Int] = None,
               unitAmountDecimal: Option[BigDecimal] = None): IO[ResponseError, Price]


    def byId(priceId: String): IO[ResponseError, Price]


    def update(priceId: String,
               active: Option[Boolean] = None,
               lookupKey: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               nickname: Option[String] = None,
               transferLookupKey: Option[Boolean] = None): IO[ResponseError, Price]


    def delete(planId: String): IO[ResponseError, Deleted]


    def list(active: Option[Boolean] = None,
             currency: Option[String] = None,
             created: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default,
             endingBefore: Option[String] = None,
             limit: Option[Int] = None,
             productId: Option[String] = None,
             `type`: Option[String] = None): IO[ResponseError, StripeList[Price]]
}