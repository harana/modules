package com.harana.modules.shopify.models

import java.time.Instant

case class Fulfilment(id: Long,
                      orderId: Long,
                      status: String,
                      createdAt: Instant,
                      updatedAt: Instant,
                      trackingCompany: Option[String],
                      trackingNumber: Option[String],
                      lineItems: List[LineItem],
                      trackingUrl: Option[String],
                      trackingUrls: List[String],
                      locationId: Option[Long])