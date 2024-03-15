package com.harana.modules.shopify.models

case class InventoryLevel(inventoryItemId: Long,
                          locationId: Option[Long],
                          available: Long)