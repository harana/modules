package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Artifactory(url: String,
                       path: Option[String],
                       username: Option[String],
                       password: Option[String])
