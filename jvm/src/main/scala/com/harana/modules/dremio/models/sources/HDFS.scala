package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.Property
import io.circe.generic.JsonCodec

@JsonCodec
case class HDFS(hostname: String,
                port: String,
                kerberosPrincipal: String,
                enableSasl: Option[Boolean],
                propertyList: List[Property])