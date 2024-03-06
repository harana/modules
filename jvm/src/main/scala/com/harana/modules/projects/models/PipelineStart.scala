package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class PipelineStart(action: String,
                         triggers: Option[List[Trigger]])