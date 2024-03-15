package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.Property
import io.circe.generic.JsonCodec

@JsonCodec
case class AWSS3(accessKey: String,
                 accessSecret: String,
                 secure: Boolean,
                 externalBucketList: List[String],
                 propertyList: List[Property])
