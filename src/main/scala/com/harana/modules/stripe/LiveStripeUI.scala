package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.http.Http
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.{Task, ZIO, ZLayer}

object LiveStripeUI {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      http          <- ZIO.service[Http]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeUI(config, http, logger, micrometer)
  }
}

case class LiveStripeUI(config: Config, http: Http, logger: Logger, micrometer: Micrometer) extends StripeUI {

  def billingPortalUrl(customerId: String, returnUrl: String): Task[String] =
    for {
      apiKey      <- config.secret("stripe-secret-key")
      formBody    =  Map(
                        "customer" -> customerId,
                        "return_url" -> returnUrl
                      )
      response    <- http.postFormAsJson("https://api.stripe.com/v1/billing_portal/sessions", formBody, credentials = Some((apiKey, ""))).mapError(e => new Exception(e.toString))
      url         <- ZIO.fromTry(response.hcursor.downField("url").as[String].toTry)
    } yield url


  def createCheckoutSession(customerId: String, priceId: String, successUrl: String, cancelUrl: String): Task[String] =
    for {
      apiKey      <- config.secret("stripe-secret-key")
      formBody    =  Map(
                      "cancel_url" -> cancelUrl,
                      "customer" -> customerId,
                      "line_items[][price]" -> priceId,
                      "line_items[][quantity]" -> "1",
                      "mode" -> "subscription",
                      "payment_method_types[]" -> "card",
                      "success_url" -> successUrl
                    )
      response    <- http.postFormAsJson("https://api.stripe.com/v1/checkout/sessions", formBody, credentials = Some((apiKey, ""))).mapError(e => new Exception(e.toString))
      id          <- ZIO.fromTry(response.hcursor.downField("id").as[String].toTry)
    } yield id

}
