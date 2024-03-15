package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Auto(repository: Option[String],
                path: Option[String],
                builder: Option[String])