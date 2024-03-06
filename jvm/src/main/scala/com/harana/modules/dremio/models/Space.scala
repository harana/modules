package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Space(entityType: String = "space",
                 id: String,
                 name: String,
                 tag: String,
                 path: String = "",
                 children: List[EntitySummary]) extends Entity