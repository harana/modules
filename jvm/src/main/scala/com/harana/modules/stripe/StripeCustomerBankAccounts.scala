package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.charge.BankAccount
import zio.IO
import zio.macros.accessible

@accessible
trait StripeCustomerBankAccounts {
   def create(customerId: String,
               source: Option[String] = None,
               defaultForCurrency: Option[String] = None,
               metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount]

    def byId(customerId: String, bankAccountId: String): IO[ResponseError, BankAccount]

    def update(customerId: String,
               bankAccountId: String,
               accountHolderName: Option[String] = None,
               accountHolderType: Option[String] = None,
               metadata: Map[String, String] = Map.empty): IO[ResponseError, BankAccount]

    def verify(customerId: String,
               bankAccountId: String,
               amount1: Option[Money] = None,
               amount2: Option[Money] = None,
               verificationMethod: Option[String] = None): IO[ResponseError, BankAccount]

    def delete(customerId: String, bankAccountId: String): IO[ResponseError, Deleted]

    def list(customerId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[BankAccount]]
}