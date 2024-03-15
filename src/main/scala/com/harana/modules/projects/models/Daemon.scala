package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Daemon(name: String,
                  minReadySeconds: Option[Int],
                  replicas: Option[Int],
                  revisionHistoryLimit: Option[Int],
                  containers: List[Container],
                  start: Option[DaemonStart],
                  strategy: Option[Strategy])