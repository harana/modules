package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.StandardAuthType
import io.circe.generic.JsonCodec

@JsonCodec
case class AWSRedshift(username: String,
                       password: String,
                       authenticationType: StandardAuthType,
                       fetchSize: Int,
                       connectionString: String)