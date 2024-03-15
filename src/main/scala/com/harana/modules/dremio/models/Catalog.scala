package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Catalog(data: List[EntitySummary])
