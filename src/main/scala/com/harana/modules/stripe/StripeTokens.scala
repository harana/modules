package com.harana.modules.stripe

import com.outr.stripe.ResponseError
import com.outr.stripe.charge.{BankAccount, Card, PII}
import com.outr.stripe.token.Token
import zio.IO
import zio.macros.accessible

@accessible
trait StripeTokens {
   def create(card: Option[Card] = None,
               bankAccount: Option[BankAccount] = None,
               pii: Option[PII] = None,
               customerId: Option[String] = None): IO[ResponseError, Token]

    def byId(tokenId: String): IO[ResponseError, Token]
}