package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Action(name: String,
                  container: Container,
                  parameters: Option[List[Parameter]],
                  dependencies: Option[List[String]],
                  withItems: Option[List[String]])