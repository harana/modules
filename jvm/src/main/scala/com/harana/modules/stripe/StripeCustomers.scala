package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.charge.{Address, Card, Shipping}
import com.outr.stripe.customer.Customer
import zio.IO
import zio.macros.accessible

@accessible
trait StripeCustomers {
   def create(address: Option[Address] = None,
               balance: Option[Money] = None,
               coupon: Option[String] = None,
               description: Option[String] = None,
               email: Option[String] = None,
               invoicePrefix: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               name: Option[String] = None,
               nextInvoiceSequence: Option[Int] = None,
               paymentMethodId: Option[String] = None,
               phone: Option[String] = None,
               promotionCode: Option[String] = None,
               shipping: Option[Shipping] = None,
               source: Option[Card] = None,
               taxExempt: Option[String] = None): IO[ResponseError, Customer]

    def byId(customerId: String): IO[ResponseError, Customer]

    def update(customerId: String,
               address: Option[Address] = None,
               balance: Option[Money] = None,
               coupon: Option[String] = None,
               defaultSource: Option[String] = None,
               description: Option[String] = None,
               email: Option[String] = None,
               invoicePrefix: Option[String] = None,
               metadata: Map[String, String] = Map.empty,
               name: Option[String] = None,
               nextInvoiceSequence: Option[Int] = None,
               phone: Option[String] = None,
               promotionCode: Option[String] = None,
               shipping: Option[Shipping] = None,
               source: Option[Card] = None,
               taxExempt: Option[String] = None): IO[ResponseError, Customer]

    def delete(customerId: String): IO[ResponseError, Deleted]

    def list(created: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default,
             email: Option[String] = None): IO[ResponseError, StripeList[Customer]]
}