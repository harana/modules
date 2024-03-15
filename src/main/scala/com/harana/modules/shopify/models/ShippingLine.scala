package com.harana.modules.shopify.models

case class ShippingLine(id: Long,
                        title: String,
                        price: BigDecimal,
                        code: String,
                        source: String)