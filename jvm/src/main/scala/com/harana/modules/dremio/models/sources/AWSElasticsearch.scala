package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.{AWSElasticsearchAuthType, AWSElasticsearchEncryptionValidationMode}
import io.circe.generic.JsonCodec

@JsonCodec
case class AWSElasticsearch(hostname: String,
                            port: Int,
                            authenticationType: AWSElasticsearchAuthType,
                            accessKey: String,
                            accessSecret: String,
                            overwriteRegion: Boolean,
                            regionName: String,
                            scriptsEnabled: Boolean,
                            showHiddenIndices: Boolean,
                            showIdColumn: Boolean,
                            readTimeoutMillis: Long,
                            scrollTimeoutMillis: Long,
                            usePainless: Boolean,
                            scrollSize: Int,
                            allowPushdownOnNormalizedOrAnalyzedFields: Boolean,
                            warnOnRowCountMismatch: Boolean,
                            encryptionValidationMode: AWSElasticsearchEncryptionValidationMode)