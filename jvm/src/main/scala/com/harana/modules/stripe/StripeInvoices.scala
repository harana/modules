package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.subscription.{Invoice, InvoiceLine}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeInvoices {
    def create(customerId: String,
               applicationFee: Option[Money] = None,
               description: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               statementDescriptor: Option[String] = None,
               subscription: Option[String] = None,
               taxPercent: Option[BigDecimal] = None): IO[ResponseError, Invoice]

    def byId(invoiceId: String): IO[ResponseError, Invoice]

    def linesById(invoiceId: String,
                  coupon: Option[String] = None,
                  customer: Option[String] = None,
                  subscription: Option[String] = None,
                  subscriptionPlan: Option[String] = None,
                  subscriptionProrate: Option[String] = None,
                  subscriptionProrationDate: Option[Long] = None,
                  subscriptionQuantity: Option[Int] = None,
                  subscriptionTrialEnd: Option[Long] = None,
                  config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[InvoiceLine]]

    def upcoming(customerId: String,
                 coupon: Option[String] = None,
                 subscription: Option[String] = None,
                 subscriptionPlan: Option[String] = None,
                 subscriptionProrate: Option[String] = None,
                 subscriptionProrationDate: Option[Long] = None,
                 subscriptionQuantity: Option[Int] = None,
                 subscriptionTrialEnd: Option[Long] = None): IO[ResponseError, Invoice]

    def update(invoiceId: String,
               applicationFee: Option[Money] = None,
               closed: Option[Boolean] = None,
               description: Option[String] = None,
               forgiven: Option[Boolean] = None,
               metadata: Map[String, String] = Map.empty,
               statementDescriptor: Option[String] = None,
               taxPercent: Option[BigDecimal] = None): IO[ResponseError, Invoice]

    def pay(invoiceId: String): IO[ResponseError, Invoice]

    def list(customerId: Option[String] = None,
             date: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Invoice]]
}