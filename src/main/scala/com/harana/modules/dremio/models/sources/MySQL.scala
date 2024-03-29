package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.StandardAuthType
import io.circe.generic.JsonCodec

@JsonCodec
case class MySQL(username: String,
                 password: String,
                 hostname: String,
                 port: String,
                 authenticationType: StandardAuthType,
                 fetchSize: Int)