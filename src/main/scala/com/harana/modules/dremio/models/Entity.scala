package com.harana.modules.dremio.models

abstract class Entity {
  val entityType: String
  val id: String
  val path: String
  val tag: String
}