package com.harana.modules.stripe

import com.outr.stripe.connect.ApplicationFee
import com.outr.stripe.{QueryConfig, ResponseError, StripeList, TimestampFilter}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeApplicationFees {
   def byId(feeId: String): IO[ResponseError, ApplicationFee]

    def list(charge: Option[String] = None,
             created: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[ApplicationFee]]
}