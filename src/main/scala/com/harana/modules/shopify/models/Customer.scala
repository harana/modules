package com.harana.modules.shopify.models

import java.time.Instant

case class Customer(id: Long,
                    email: String,
                    acceptsMarketing: Boolean,
                    createdAt: Instant,
                    updatedAt: Instant,
                    firstName: String,
                    lastName: String,
                    phone: Option[String],
                    ordersCount: Long,
                    state: String,
                    totalSpent: BigDecimal,
                    note: Option[String],
                    verifiedEmail: Option[Boolean],
                    addresses: Set[Address])