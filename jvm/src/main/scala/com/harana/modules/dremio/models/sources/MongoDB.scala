package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.{Host, Property, StandardAuthType}
import io.circe.generic.JsonCodec

@JsonCodec
case class MongoDB(username: String,
                   password: String,
                   hostList: List[Host],
                   useSsl: Boolean,
                   authenticationType: StandardAuthType,
                   authDatabase: String,
                   authenticationTimeoutMillis: Long,
                   secondaryReadsOnly: Boolean,
                   subpartitionSize: Int,
                   propertyList: List[Property])