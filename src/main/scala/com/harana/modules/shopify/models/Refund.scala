package com.harana.modules.shopify.models

import java.time.Instant

case class Refund(id: Long,
                  orderId: Long,
                  createdAt: Instant,
                  note: Option[String],
                  userId: Option[Long] ,
                  processedAt: Instant,
                  refundLineItems: List[RefundLineItem],
                  shipping: Option[RefundShippingDetails],
                  transactions: List[Transaction],
                  currency: Option[String])