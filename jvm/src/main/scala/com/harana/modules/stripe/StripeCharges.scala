package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.charge.{Charge, FraudDetails, Shipping}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeCharges {
   def create(amount: Money,
               currency: String,
               applicationFee: Option[Money] = None,
               capture: Boolean = true,
               description: Option[String] = None,
               destination: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               receiptEmail: Option[String] = None,
               shipping: Option[Shipping] = None,
               customer: Option[String] = None,
               source: Option[String] = None,
               statementDescriptor: Option[String] = None): IO[ResponseError, Charge]

    def byId(chargeId: String): IO[ResponseError, Charge]

    def update(chargeId: String,
               description: Option[String] = None,
               fraudDetails: Option[FraudDetails] = None,
               metadata: Map[String, String] = Map.empty,
               receiptEmail: Option[String] = None,
               shipping: Option[Shipping] = None): IO[ResponseError, Charge]

    def capture(chargeId: String,
                amount: Option[Money] = None,
                applicationFee: Option[Money] = None,
                receiptEmail: Option[String] = None,
                statementDescriptor: Option[String] = None): IO[ResponseError, Charge]

    def list(created: Option[TimestampFilter] = None,
             customer: Option[String] = None,
             source: Option[String] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Charge]]
}