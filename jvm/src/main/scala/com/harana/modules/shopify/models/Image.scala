package com.harana.modules.shopify.models

case class Image(id: Long,
                 productId: Long,
                 name: Option[String],
                 position: Int,
                 source: Option[String],
                 variantIds: List[Long])