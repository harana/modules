package com.harana.designer.backend.modules.projects.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Notification(name: String,
                        email: Option[Email],
                        slack: Option[NotificationSlack])
