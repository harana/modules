package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class DatasetFieldType(name: DatasetFieldName,
                            subSchema: DatasetField,
                            precision: Int,
                            scale: Int)
