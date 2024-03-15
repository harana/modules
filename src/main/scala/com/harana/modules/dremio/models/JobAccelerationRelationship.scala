package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class JobAccelerationRelationship(reflectionId: String,
                                       datasetId: String,
                                       relationship: JobAccelerationRelationshipType)
