package com.harana.modules.shopify.models

case class ProductOption(id: Long,
                         productId: Long,
                         name: String,
                         position: Int,
                         values: List[String])