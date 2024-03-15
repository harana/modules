package com.harana.modules.stripe

import com.outr.stripe.connect.CountrySpec
import com.outr.stripe.{QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeCountrySpecs {
   def list(config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[CountrySpec]]

    def byId(countryCode: String): IO[ResponseError, CountrySpec]
}