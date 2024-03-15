package com.harana.modules

import io.circe.generic.JsonCodec

package object docker {

  type ContainerId = String
  type EventId = String
  type ExecId = String
  type ImageId = String
  type NetworkId = String
  type ServiceId = String
  type SwarmId = String
  type TaskId = String

  @JsonCodec
  case class HubPage(count: Int,
                     next: Option[String],
                     previous: Option[String],
                     results: List[HubTag])


  @JsonCodec
  case class HubTag(creator: Long,
                    id: Long,
                    images: List[HubImage] = List(),
                    last_updated: String,
                    last_updater: Long,
                    last_updater_username: String,
                    name: String,
                    repository: Long,
                    full_size: Long,
                    v2: Boolean,
                    tag_status: String,
                    tag_last_pulled: Option[String],
                    tag_last_pushed: Option[String],
                    media_type: String,
                    digest: String)

  @JsonCodec
  case class HubImage(architecture: String,
                      features: String,
                      variant: Option[String],
                      digest: String,
                      os: String,
                      os_features: String,
                      os_version: Option[String],
                      size: Long,
                      status: String,
                      last_pulled: Option[String],
                      last_pushed: Option[String])

}
