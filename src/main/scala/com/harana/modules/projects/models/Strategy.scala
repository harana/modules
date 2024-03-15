package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Strategy(blueGreen: Option[BlueGreen],
                    canary: Option[Canary])