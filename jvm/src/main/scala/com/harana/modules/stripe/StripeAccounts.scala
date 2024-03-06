package com.harana.modules.stripe

import com.outr.stripe.connect._
import com.outr.stripe.{Deleted, QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeAccounts {
   def create(country: Option[String] = None,
               email: Option[String] = None,
               custom: Boolean = false,
               accountToken: Option[String] = None,
               businessLogo: Option[String] = None,
               businessName: Option[String] = None,
               businessPrimaryColor: Option[String] = None,
               businessURL: Option[String] = None,
               legalEntity: Option[LegalEntity] = None,
               tosAcceptance: Option[Acceptance] = None): IO[ResponseError, Account]

      def byId(accountId: String): IO[ResponseError, Account]

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
                 transferStatementDescriptor: Option[String] = None): IO[ResponseError, Account]

      def delete(accountId: String): IO[ResponseError, Deleted]

      def reject(accountId: String, reason: String): IO[ResponseError, Account]

      def list(config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Account]]
}