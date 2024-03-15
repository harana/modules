package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.subscription.InvoiceItem
import zio.IO
import zio.macros.accessible

@accessible
trait StripeInvoiceItems {
   def create(amount: Money,
               currency: String,
               customerId: String,
               description: Option[String] = None,
               discountable: Option[Boolean] = None,
               invoice: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               subscription: Option[String] = None): IO[ResponseError, InvoiceItem]

    def byId(invoiceItemId: String): IO[ResponseError, InvoiceItem]

    def update(invoiceItemId: String,
               amount: Option[Money] = None,
               description: Option[String] = None,
               discountable: Option[Boolean] = None,
               metadata: Map[String, String] = Map.empty): IO[ResponseError, InvoiceItem]

    def delete(invoiceItemId: String): IO[ResponseError, Deleted]

    def list(created: Option[TimestampFilter] = None,
             customer: Option[String] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[InvoiceItem]]
}