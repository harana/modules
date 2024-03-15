package com.harana.modules.stripe

import zio.Task
import zio.macros.accessible

@accessible
trait StripeUI {
  def billingPortalUrl(customerId: String, returnUrl: String): Task[String]

  def createCheckoutSession(customerId: String, priceId: String, successUrl: String, cancelUrl: String): Task[String]
}