package com.harana.modules.stripe

import com.outr.stripe.{Deleted, ResponseError}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeDiscounts {
   def deleteCustomerDiscount(customerId: String): IO[ResponseError, Deleted]

    def deleteSubscriptionDiscount(subscriptionId: String): IO[ResponseError, Deleted]
}