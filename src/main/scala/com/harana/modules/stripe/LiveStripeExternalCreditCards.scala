package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.charge.Card
import zio.{IO, ZIO, ZLayer}

object LiveStripeExternalCreditCards {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeExternalCreditCards(config, logger, micrometer)
  }
}

case class LiveStripeExternalCreditCards(config: Config, logger: Logger, micrometer: Micrometer) extends StripeExternalCreditCards {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).accounts.external.cards)

  def create(accountId: String,
             source: Option[String] = None,
             externalAccount: Option[String] = None,
             defaultForCurrency: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, Card] =
    for {
      c <- client
      r <- execute(c.create(accountId, source, externalAccount, defaultForCurrency, metadata))
    } yield r


  def byId(accountId: String, cardId: String): IO[ResponseError, Card] =
    for {
      c <- client
      r <- execute(c.byId(accountId, cardId))
    } yield r


  def update(accountId: String,
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
    r <- execute(c.update(accountId, cardId, addressCity, addressCountry, addressLine1, addressLine2, addressState, addressZip,
      defaultForCurrency, expMonth, expYear, metadata, name))
  } yield r


  def delete(accountId: String, cardId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(accountId, cardId))
    } yield r


  def list(accountId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Card]] =
    for {
      c <- client
      r <- execute(c.list(accountId, config))
    } yield r

}
