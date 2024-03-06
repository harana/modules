package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Folder(entityType: String = "folder",
                  id: String,
                  path: String,
                  tag: String,
                  children: List[EntitySummary]) extends Entity