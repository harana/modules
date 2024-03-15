package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Container(name: String,
                     arguments: Option[List[String]],
                     auto: Option[Auto],
                     command: Option[List[String]],
                     docker: Option[Docker],
                     environmentVariables: Option[List[EnvironmentVariable]],
                     imagePullPolicy: Option[String],
                     ports: Option[List[Port]],
                     python: Option[Python],
                     resources: Option[Resources],
                     scala: Option[Scala],
                     version: Option[String],
                     volumeMounts: Option[List[VolumeMount]])