package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.charge.Card
import zio.{IO, ZIO, ZLayer}

object LiveStripeCustomerCreditCards {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeCustomerCreditCards(config, logger, micrometer)
  }
}

case class LiveStripeCustomerCreditCards(config: Config, logger: Logger, micrometer: Micrometer) extends StripeCustomerCreditCards {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).customers.sources.cards)

  def create(customerId: String,
             source: Option[String] = None,
             externalAccount: Option[String] = None,
             defaultForCurrency: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, Card] =
          for {
      c <- client
      r <- execute(c.create(customerId, source, externalAccount, defaultForCurrency, metadata))
    } yield r


  def byId(customerId: String, cardId: String): IO[ResponseError, Card] =
          for {
      c <- client
      r <- execute(c.byId(customerId, cardId))
    } yield r


  def update(customerId: String,
             cardId: String,
             addressCity: Option[String] = None,
             addressCountry: Option[String] = None,
             addressLine1: Option[String] = None,
             addressLine2: Option[String] = None,
             addressState: Option[String] = None,
             addressZip: Option[String] = None,
             defaultForCurrency: Option[String] = None,
             expMonth: Option[Int] = None,
             expYear: Option[Int] = None,
             metadata: Map[String, String] = Map.empty,
             name: Option[String] = None): IO[ResponseError, Card] =
    for {
      c <- client
      r <- execute(c.update(customerId, cardId, addressCity, addressCountry, addressLine1, addressLine2, addressState, addressZip, defaultForCurrency,
        expMonth, expYear, metadata, name))
    } yield r

  def delete(customerId: String, cardId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(customerId, cardId))
    } yield r

  def list(customerId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Card]] =
    for {
      c <- client
      r <- execute(c.list(customerId, config))
    } yield r

}