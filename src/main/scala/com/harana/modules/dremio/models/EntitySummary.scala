package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class EntitySummary(id: String,
                         path: String,
                         tag: String,
                         `type`: String,
                         datasetType: String,
                         containerType: String)