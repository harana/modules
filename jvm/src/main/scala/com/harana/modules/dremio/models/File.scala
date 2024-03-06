package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class File(entityType: String = "file",
                id: String,
                path: String,
                tag: String) extends Entity