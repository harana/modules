package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Pipeline(name: String,
                    start: PipelineStart,
                    actions: List[Action])