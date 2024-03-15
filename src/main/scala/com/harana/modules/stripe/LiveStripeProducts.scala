package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.product.{PackageDimensions, Product => StripeProduct}
import zio.{IO, ZIO, ZLayer}

object LiveStripeProducts {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeProducts(config, logger, micrometer)
  }
}

case class LiveStripeProducts(config: Config, logger: Logger, micrometer: Micrometer) extends StripeProducts {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).products)

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
             url: Option[String] = None): IO[ResponseError, StripeProduct] =
    for {
      c <- client
      r <- execute(c.create(name, active, attributes, caption, deactivateOn, description, images, liveMode, metadata, packageDimensions, productId, shippable, statementDescriptor, `type`, unitLabel, url))
    } yield r


  def byId(productId: String): IO[ResponseError, StripeProduct] =
    for {
      c <- client
      r <- execute(c.byId(productId))
    } yield r


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
             url: Option[String] = None): IO[ResponseError, StripeProduct] =
    for {
      c <- client
      r <- execute(c.update(productId, active, attributes, caption, deactivateOn, description, images, liveMode, metadata, name, packageDimensions, shippable, statementDescriptor, `type`, unitLabel, url))
    } yield r


  def delete(productId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(productId))
    } yield r


  def list(active: Option[Boolean] = None,
           created: Option[TimestampFilter] = None,
           config: QueryConfig = QueryConfig.default,
           ids: List[String] = Nil,
           shippable: Option[Boolean] = None,
           `type`: Option[String] = None,
           url: Option[String] = None): IO[ResponseError, StripeList[StripeProduct]] =
    for {
      c <- client
      r <- execute(c.list(active, created, config, ids, shippable, `type`, url))
    } yield r

}