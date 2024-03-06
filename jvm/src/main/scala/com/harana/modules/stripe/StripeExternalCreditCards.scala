package com.harana.modules.stripe

import com.outr.stripe.charge.Card
import com.outr.stripe.{Deleted, QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeExternalCreditCards {
  def create(accountId: String,
               source: Option[String] = None,
               externalAccount: Option[String] = None,
               defaultForCurrency: Option[String] = None,
               metadata: Map[String, String] = Map.empty): IO[ResponseError, Card]

    def byId(accountId: String, cardId: String): IO[ResponseError, Card]

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
               name: Option[String] = None): IO[ResponseError, Card]

    def delete(accountId: String, cardId: String): IO[ResponseError, Deleted]

    def list(accountId: String, config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Card]]
}