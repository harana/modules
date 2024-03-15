package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Canary(stableService: String,
                  canaryService: String,
                  steps: List[CanaryStep] = List(),
                  maxSurge: Option[String] = None,
                  maxUnavailable: Option[String] = None)