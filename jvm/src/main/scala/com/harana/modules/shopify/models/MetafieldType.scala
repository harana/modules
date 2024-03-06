package com.harana.modules.shopify.models

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class MetafieldType(val value: String) extends StringEnumEntry

object MetafieldType extends StringEnum[MetafieldType] with StringCirceEnum[MetafieldType] {
  case object Boolean extends MetafieldType("boolean")
  case object Color extends MetafieldType("color")
  case object Date extends MetafieldType("date")
  case object DateTime extends MetafieldType("date_time")
  case object Dimension extends MetafieldType("dimension")
  case object Json extends MetafieldType("json")
  case object Money extends MetafieldType("money")
  case object MultiLineTextField extends MetafieldType("multi_line_text_field")
  case object NumberDecimal extends MetafieldType("number_decimal")
  case object NumberInteger extends MetafieldType("number_integer")
  case object Rating extends MetafieldType("rating")
  case object RichTextField extends MetafieldType("rich_text_field")
  case object SingleLineTextField extends MetafieldType("single_line_text_field")
  case object Url extends MetafieldType("url")
  case object Volume extends MetafieldType("volume")
  case object Weight extends MetafieldType("weight")

  case object CollectionReference extends MetafieldType("collection_reference")
  case object FileReference extends MetafieldType("file_reference")
  case object MetaobjectReference extends MetafieldType("metaobject_reference")
  case object MixedReference extends MetafieldType("mixed_reference")
  case object PageReference extends MetafieldType("page_reference")
  case object ProductReference extends MetafieldType("product_reference")
  case object VariantReference extends MetafieldType("variant_reference")

  case object ListCollectionReference extends MetafieldType("list.collection_reference")
  case object ListColor extends MetafieldType("list.color")
  case object ListDate extends MetafieldType("list.date")
  case object ListDateTime extends MetafieldType("list.date_time")
  case object ListDimension extends MetafieldType("list.dimension")
  case object ListFileReference extends MetafieldType("list.file_reference")
  case object ListMetaobjectReference extends MetafieldType("list.metaobject_reference")
  case object ListMixedReference extends MetafieldType("list.mixed_reference")
  case object ListNumberInteger extends MetafieldType("list.number_integer")
  case object ListNumberDecimal extends MetafieldType("list.number_decimal")
  case object ListPageReference extends MetafieldType("list.page_reference")
  case object ListProductReference extends MetafieldType("list.product_reference")
  case object ListRating extends MetafieldType("list.rating")
  case object ListSingleLineTextField extends MetafieldType("list.single_line_text_field")
  case object ListUrl extends MetafieldType("list.url")
  case object ListVariantReference extends MetafieldType("list.variant_reference")
  case object ListVolume extends MetafieldType("list.volume")
  case object ListWeight extends MetafieldType("list.weight")

  val values = findValues
}