package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class BlueGreen(activeService: String,
                       previewService: String,
                       previewReplicaCount: Option[Int],
                       autoPromotionEnabled: Option[Boolean],
                       autoPromotionSeconds: Option[Int],
                       scaleDownDelaySeconds: Option[Int],
                       scaleDownDelayRevisionLimit: Option[Int])
