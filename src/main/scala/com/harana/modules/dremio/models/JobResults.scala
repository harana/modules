package com.harana.modules.dremio.models

import io.circe.Json
import io.circe.generic.JsonCodec

@JsonCodec
case class JobResults(rowCount: Int,
                      schema: List[DatasetField],
                      rows: List[Json])