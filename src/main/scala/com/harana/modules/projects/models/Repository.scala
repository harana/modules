package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Repository(name: String,
                      git: Git)