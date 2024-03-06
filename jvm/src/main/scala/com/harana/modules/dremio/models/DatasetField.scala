package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class DatasetField(name: String,
                        `type`: DatasetFieldType)