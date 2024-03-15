package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.Property
import io.circe.generic.JsonCodec

@JsonCodec
case class Snowflake(account: String,
                     username: String,
                     password: String,
                     propertyList: List[Property])