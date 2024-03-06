package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.transfer.Transfer
import zio.IO
import zio.macros.accessible

@accessible
trait StripeTransfers {
   def create(amount: Money,
             currency: String,
             destination: String,
             applicationFee: Option[Money] = None,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             sourceTransaction: Option[String] = None,
             statementDescriptor: Option[String] = None,
             sourceType: String = "card",
             method: String = "standard"): IO[ResponseError, Transfer]

  def byId(transferId: String): IO[ResponseError, Transfer]

  def update(transferId: String,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, Transfer]

  def list(created: Option[TimestampFilter] = None,
           date: Option[TimestampFilter] = None,
           destination: Option[String] = None,
           recipient: Option[String] = None,
           status: Option[String] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Transfer]]
}