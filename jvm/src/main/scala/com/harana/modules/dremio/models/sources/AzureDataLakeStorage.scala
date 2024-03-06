package com.harana.modules.dremio.models.sources

import io.circe.generic.JsonCodec

@JsonCodec
case class AzureDataLakeStorage(mode: String = "CLIENT_KEY",
                                accountName: String,
                                clientId: String,
                                clientKeyRefreshUrl: String,
                                clientKeyPassword: String)