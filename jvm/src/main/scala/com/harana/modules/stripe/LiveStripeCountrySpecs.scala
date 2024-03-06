package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe.connect.CountrySpec
import com.outr.stripe.{QueryConfig, ResponseError, Stripe, StripeList}
import zio.{IO, ZIO, ZLayer}

object LiveStripeCountrySpecs {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeCountrySpecs(config, logger, micrometer)
  }
}

case class LiveStripeCountrySpecs(config: Config, logger: Logger, micrometer: Micrometer) extends StripeCountrySpecs {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).countrySpecs)

  def list(config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[CountrySpec]] =
    for {
      c <- client
      r <- execute(c.list(config))
    } yield r


  def byId(countryCode: String): IO[ResponseError, CountrySpec] =
    for {
      c <- client
      r <- execute(c.byId(countryCode))
    } yield r

}