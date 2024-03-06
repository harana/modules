package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.product.{PackageDimensions, Product => StripeProduct}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeProducts {
   def create(name: String,
               active: Option[Boolean] = None,
               attributes: List[String] = List.empty,
               caption: Option[String] = None,
               deactivateOn: List[String] = List.empty,
               description: Option[String] = None,
               images: List[String] = List.empty,
               liveMode: Option[Boolean] = None,
               metadata: Map[String, String] = Map.empty,
               packageDimensions: Option[PackageDimensions] = None,
               productId: Option[String] = None,
               shippable: Option[Boolean] = None,
               statementDescriptor: Option[String] = None,
               `type`: Option[String] = None,
               unitLabel: Option[String] = None,
               url: Option[String] = None): IO[ResponseError, StripeProduct]

    def byId(productId: String): IO[ResponseError, StripeProduct]

    def update(productId: String,
               active: Option[Boolean] = None,
               attributes: List[String] = List.empty,
               caption: Option[String] = None,
               deactivateOn: List[String] = List.empty,
               description: Option[String] = None,
               images: List[String] = List.empty,
               liveMode: Option[Boolean] = None,
               metadata: Map[String, String] = Map.empty,
               name: Option[String] = None,
               packageDimensions: Option[PackageDimensions] = None,
               shippable: Option[Boolean] = None,
               statementDescriptor: Option[String] = None,
               `type`: Option[String] = None,
               unitLabel: Option[String] = None,
               url: Option[String] = None): IO[ResponseError, StripeProduct]

    def delete(productId: String): IO[ResponseError, Deleted]

    def list(active: Option[Boolean] = None,
             created: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default,
             ids: List[String] = Nil,
             shippable: Option[Boolean] = None,
             `type`: Option[String] = None,
             url: Option[String] = None): IO[ResponseError, StripeList[StripeProduct]]
}