package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Scala(repository: Option[String],
                 path: Option[String],
                 baseImage: Option[String],
                 sbt: Option[SBT])