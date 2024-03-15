package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Dataset(entityType: String = "dataset",
                   id: String,
                   path: String,
                   tag: String,
                   `type`: DatasetType,
                   fields: List[DatasetField],
                   createdAt: String,
                   accelerationRefreshPolicy: Option[AccelerationRefreshPolicy],
                   sql: Option[String],
                   sqlContext: String,
// FIXME
//                   format: DatasetFormat,
                   approximateStatisticsAllowed: Boolean) extends Entity
