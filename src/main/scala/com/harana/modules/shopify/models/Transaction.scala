package com.harana.modules.shopify.models

case class Transaction(orderId: Long,
                       kind: String,
                       gateway: String,
                       parentId: Long,
                       amount: BigDecimal,
                       currency: Option[String],
                       maximumRefundable: Option[BigDecimal],
                       receipt: TransactionReceipt)