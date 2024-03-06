package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Project(title: String,
                   description: String,
                   author: Author,
                   parameters: Option[List[Parameter]],
                   repositories: Option[List[Repository]],
                   containers: List[Container],
                   pipelines: Option[List[Pipeline]],
                   daemons: Option[List[Daemon]],
                   notifications: Option[List[Notification]])