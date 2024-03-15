package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class SourceMetadataPolicy(authTTLMs: Long,
                                datasetRefreshAfterMs: Long,
                                datasetExpireAfterMs: Long,
                                namesRefreshMs: Long,
                                datasetUpdateMode: DatasetUpdateMode)