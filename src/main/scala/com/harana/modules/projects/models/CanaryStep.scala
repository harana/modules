package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class CanaryStep(weight: Int,
                      pause: Option[Int])
