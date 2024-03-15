package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.Property
import io.circe.generic.JsonCodec

@JsonCodec
case class AzureStorage(accountKind: String,
                        accountName: String,
                        accessKey: String,
                        enableSSL: Boolean,
                        rootPath: String,
                        containers: List[String],
                        propertyList: List[Property])