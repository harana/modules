package com.harana.modules.shopify.models

case class RefundShippingDetails(amount: BigDecimal,
                                 tax: BigDecimal,
                                 maximumRefundable: Option[BigDecimal],
                                 fullRefund: Boolean)
