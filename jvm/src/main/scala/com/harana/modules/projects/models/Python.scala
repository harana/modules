package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Python(repository: Option[String],
                  path: Option[String],
                  file: Option[String],
                  baseImage: Option[String])