package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.charge.BankAccount
import zio.{IO, ZIO, ZLayer}

object LiveStripeExternalBankAccounts {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeExternalBankAccounts(config, logger, micrometer)
  }
}

case class LiveStripeExternalBankAccounts(config: Config, logger: Logger, micrometer: Micrometer) extends StripeExternalBankAccounts {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).accounts.external.bankAccounts)

  def create(accountId: String,
             source: Option[String] = None,
             externalAccount: Option[String] = None,
             defaultForCurrency: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.create(accountId, source, externalAccount, defaultForCurrency ,metadata))
    } yield r


  def byId(accountId: String, bankAccountId: String): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.byId(accountId, bankAccountId))
    } yield r


  def update(accountId: String,
             bankAccountId: String,
             defaultForCurrency: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.update(accountId, bankAccountId, defaultForCurrency, metadata))
    } yield r


  def delete(accountId: String, bankAccountId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(accountId, bankAccountId))
    } yield r


  def list(accountId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[BankAccount]] =
    for {
      c <- client
      r <- execute(c.list(accountId, config))
    } yield r

}
