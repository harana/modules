package com.harana.modules.shopify.models

import java.time.Instant

case class Product(id: Long,
                   title: String,
                   productType: String,
                   bodyHtml: String,
                   vendor: String,
                   tags: String,
                   options: List[ProductOption],
                   metafieldsGlobalTitleTag: Option[String],
                   metafieldsGlobalDescriptionTag: Option[String],
                   images: List[Image],
                   image: Image,
                   variants: List[ProductVariant],
                   publishedAt: Option[Instant],
                   published: Option[Boolean])