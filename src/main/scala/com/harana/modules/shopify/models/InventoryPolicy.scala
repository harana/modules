package com.harana.modules.shopify.models

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class InventoryPolicy(val value: String) extends StringEnumEntry

object InventoryPolicy extends StringEnum[InventoryPolicy] with StringCirceEnum[InventoryPolicy] {
  case object Continue extends InventoryPolicy("continue")
  case object Deny extends InventoryPolicy("deny")
  val values = findValues
}