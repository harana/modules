package com.harana.modules.dremio.models.sources

import com.harana.modules.dremio.models.{Host, StandardAuthType}
import io.circe.generic.JsonCodec

@JsonCodec
case class Elasticsearch(username: String,
                         password: String,
                         hostList: List[Host],
                         authenticationType: StandardAuthType,
                         scriptsEnabled: Option[Boolean],
                         showHiddenIndices: Option[Boolean],
                         sslEnabled: Option[Boolean],
                         showIdColumn: Option[Boolean],
                         readTimeoutMillis: Option[Long],
                         scrollTimeoutMillis: Option[Long],
                         usePainless: Option[Boolean],
                         useWhitelist: Option[Boolean],
                         scrollSize: Option[Int])