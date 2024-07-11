package com.harana.modules.airbyte

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

import java.lang.{String => JString}

sealed trait AirbyteOption
object AirbyteOption {
  case class Boolean(b: Boolean) extends AirbyteOption
  case class Integer(i: Int) extends AirbyteOption
  case class Object(title: JString,
                    description: Option[JString],
                    properties: List[AirbyteProperty]) extends AirbyteOption
  case class String(s: JString) extends AirbyteOption
}

sealed abstract class AirbyteSyncDirection(val value: String) extends StringEnumEntry
object AirbyteSyncDirection extends StringEnum[AirbyteSyncDirection] with StringCirceEnum[AirbyteSyncDirection] {
  case object Source extends AirbyteSyncDirection("source")
  case object Destination extends AirbyteSyncDirection("destination")
  val values = findValues
}

sealed abstract class AirbyteSyncMode(val value: String) extends StringEnumEntry
object AirbyteSyncMode extends StringEnum[AirbyteSyncMode] with StringCirceEnum[AirbyteSyncMode] {
  case object Append extends AirbyteSyncMode("append")
  case object AppendDeduplicate extends AirbyteSyncMode("append_dedup")
  case object Overwrite extends AirbyteSyncMode("overwrite")
  val values = findValues
}

sealed abstract class AirbytePropertyType(val value: String) extends StringEnumEntry
object AirbytePropertyType extends StringEnum[AirbytePropertyType] with StringCirceEnum[AirbytePropertyType] {
  case object Boolean extends AirbytePropertyType("boolean")
  case object Integer extends AirbytePropertyType("integer")
  case object List extends AirbytePropertyType("")
  case object Object extends AirbytePropertyType("object")
  case object String extends AirbytePropertyType("string")
  case object Array extends AirbytePropertyType("array")
  val values = findValues
}

case class AirbyteProperty(name: String,
                           `type`: AirbytePropertyType,
                           title: Option[String] = None,
                           description: Option[String] = None,
                           placeholder: Option[String] = None,
                           required: Boolean = false,
                           validationPattern: Option[String] = None,
                           default: Option[Either[String, Int]] = None,
                           options: List[AirbyteOption] = List(),
                           multiline: Boolean = false,
                           minimum: Option[Int] = None,
                           maximum: Option[Int] = None,
                           order: Option[Int] = None,
                           secret: Boolean = false)

case class AirbyteIntegration(name: String,
                              properties: List[AirbyteProperty],
                              syncDirection: AirbyteSyncDirection,
                              supportsDBT: Boolean,
                              supportsIncremental: Boolean,
                              supportsNormalization: Boolean,
                              supportedSyncModes: List[AirbyteSyncMode])
