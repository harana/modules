package com.harana.modules.shopify.models

case class LineItem(id: Long,
                    variantId: Long,
                    title: String,
                    quantity: Long,
                    price: BigDecimal,
                    grams: Long,
                    sku: Option[String],
                    variantTitle: String,
                    vendor: String,
                    productId: Long,
                    requiresShipping: Boolean,
                    taxable: Boolean,
                    giftCard: Boolean,
                    name: String,
                    variantInventoryManagement: Option[String],
                    fulfillableQuantity: Long,
                    totalDiscount: BigDecimal,
                    fulfillmentStatus: Option[String],
                    fulfillmentService: String)