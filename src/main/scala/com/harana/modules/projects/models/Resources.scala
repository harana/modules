package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Resources(cpu: Option[String],
                     memory: Option[String])