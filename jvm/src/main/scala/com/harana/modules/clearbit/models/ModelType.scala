package com.harana.modules.clearbit.models

sealed trait ModelType
object ModelType {
  case object PERSON extends ModelType
  case object COMPANY extends ModelType
  case object PERSON_COMPANY extends ModelType
}