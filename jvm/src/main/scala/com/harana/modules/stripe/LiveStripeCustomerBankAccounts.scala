package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.charge.BankAccount
import zio.{IO, ZIO, ZLayer}

object LiveStripeCustomerBankAccounts {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeCustomerBankAccounts(config, logger, micrometer)
  }
}

case class LiveStripeCustomerBankAccounts(config: Config, logger: Logger, micrometer: Micrometer) extends StripeCustomerBankAccounts {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).customers.sources.bankAccounts)

  def create(customerId: String,
             source: Option[String] = None,
             defaultForCurrency: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.create(customerId, source, defaultForCurrency, metadata))
    } yield r


  def byId(customerId: String, bankAccountId: String): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.byId(customerId, bankAccountId))
    } yield r


  def update(customerId: String,
             bankAccountId: String,
             accountHolderName: Option[String] = None,
             accountHolderType: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.update(customerId, bankAccountId, accountHolderName, accountHolderType, metadata))
    } yield r


  def verify(customerId: String,
             bankAccountId: String,
             amount1: Option[Money] = None,
             amount2: Option[Money] = None,
             verificationMethod: Option[String] = None): IO[ResponseError, BankAccount] =
    for {
      c <- client
      r <- execute(c.verify(customerId, bankAccountId, amount1, amount2, verificationMethod))
    } yield r


  def delete(customerId: String, bankAccountId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(customerId, bankAccountId))
    } yield r


  def list(customerId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[BankAccount]] =
    for {
      c <- client
      r <- execute(c.list(customerId, config))
    } yield r

}