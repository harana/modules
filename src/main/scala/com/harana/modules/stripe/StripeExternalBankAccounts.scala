package com.harana.modules.stripe

import com.outr.stripe.charge.BankAccount
import com.outr.stripe.{Deleted, QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeExternalBankAccounts {
   def create(accountId: String,
               source: Option[String] = None,
               externalAccount: Option[String] = None,
               defaultForCurrency: Option[String] = None,
               metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount]

    def byId(accountId: String, bankAccountId: String): IO[ResponseError, BankAccount]

    def update(accountId: String,
               bankAccountId: String,
               defaultForCurrency: Option[String] = None,
               metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount]

    def delete(accountId: String, bankAccountId: String): IO[ResponseError, Deleted]

    def list(accountId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[BankAccount]]
}