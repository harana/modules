package com.harana.modules.stripe

import com.outr.stripe.refund.Refund
import com.outr.stripe.{Money, QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeRefunds {
   def create(chargeId: String,
               amount: Option[Money] = None,
               metadata: Map[String, String] = Map.empty,
               reason: Option[String] = None,
               refundApplicationFee: Boolean = false,
               reverseTransfer: Boolean = false): IO[ResponseError, Refund]

    def byId(refundId: String): IO[ResponseError, Refund]

    def update(refundId: String, metadata: Map[String, String] = Map.empty): IO[ResponseError, Refund]

    def list(chargeId: Option[String] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Refund]]
}