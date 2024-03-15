package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.connect._
import zio.{IO, ZIO, ZLayer}

object LiveStripeAccounts {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeAccounts(config, logger, micrometer)
  }
}

case class LiveStripeAccounts(config: Config, logger: Logger, micrometer: Micrometer) extends StripeAccounts {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).accounts)

  def create(country: Option[String] = None,
             email: Option[String] = None,
             custom: Boolean = false,
             accountToken: Option[String] = None,
             businessLogo: Option[String] = None,
             businessName: Option[String] = None,
             businessPrimaryColor: Option[String] = None,
             businessURL: Option[String] = None,
             legalEntity: Option[LegalEntity] = None,
             tosAcceptance: Option[Acceptance] = None): IO[ResponseError, Account] =
    for {
      c <- client
      r <- execute(c.create(country, email, custom, accountToken, businessLogo, businessName, businessPrimaryColor, businessURL, legalEntity, tosAcceptance))
    } yield r


  def byId(accountId: String): IO[ResponseError, Account] =
    for {
      c <- client
      r <- execute(c.byId(accountId))
    } yield r


  def update(accountId: String,
             businessLogo: Option[String] = None,
             businessName: Option[String] = None,
             businessPrimaryColor: Option[String] = None,
             businessUrl: Option[String] = None,
             debitNegativeBalances: Option[Boolean] = None,
             declineChargeOn: Option[DeclineChargeOn] = None,
             defaultCurrency: Option[String] = None,
             email: Option[String] = None,
             externalAccount: Option[String] = None,
             legalEntity: Option[LegalEntity] = None,
             metadata: Map[String, String] = Map.empty,
             productDescription: Option[String] = None,
             statementDescriptor: Option[String] = None,
             supportEmail: Option[String] = None,
             supportPhone: Option[String] = None,
             supportUrl: Option[String] = None,
             tosAcceptance: Option[Acceptance] = None,
             transferSchedule: Option[TransferSchedule] = None,
             transferStatementDescriptor: Option[String] = None): IO[ResponseError, Account] =
    for {
      c <- client
      r <- execute(c.update(accountId, businessLogo, businessName, businessPrimaryColor, businessUrl, debitNegativeBalances, declineChargeOn,
        defaultCurrency, email, externalAccount, legalEntity, metadata, productDescription, statementDescriptor, supportEmail, supportPhone, supportUrl,
        tosAcceptance, transferSchedule, transferStatementDescriptor))
    } yield r


  def delete(accountId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(accountId))
    } yield r


  def reject(accountId: String, reason: String): IO[ResponseError, Account] =
    for {
      c <- client
      r <- execute(c.reject(accountId, reason))
    } yield r


  def list(config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Account]] =
    for {
      c <- client
      r <- execute(c.list(config))
    } yield r
}