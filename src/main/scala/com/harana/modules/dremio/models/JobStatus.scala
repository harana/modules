package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class JobStatus(jobState: JobStateJobQueryType,
                     queryType: JobQueryType,
                     startedAt: String,
                     endedAt: String,
                     rowCount: Option[Int],
                     acceleration: Option[JobAccelerationStatus],
                     errorMessage: String)