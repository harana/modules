package com.harana.modules.shopify.models

case class RefundLineItem(id: Long,
                          quantity: Long,
                          lineItemId: Long,
                          locationId: Option[Long],
                          restockType: String,
                          subtotal: BigDecimal,
                          totalTax: BigDecimal,
                          lineItem: LineItem)