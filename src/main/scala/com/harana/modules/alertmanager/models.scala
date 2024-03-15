package com.harana.modules

import io.circe.generic.JsonCodec
import io.circe.{Decoder, Encoder}

package object alertmanager {

  implicit val decodeDateTime: Decoder[DateTime] =
    Decoder.decodeString.emap { str => Right(DateTime.parseRfc3339(str)) }

  implicit val encodeDateTime: Encoder[DateTime] =
    Encoder.encodeString.contramap[DateTime](_.toStringRfc3339)


  @JsonCodec
  case class Alert(labels: Map[String, String],
                   annotations: Map[String, String],
                   receivers: List[ReceiverName],
                   status: AlertStatus,
                   fingerprint: String,
                   updatedAt: DateTime,
                   startsAt: DateTime,
                   endsAt: DateTime,
                   generatorURL: String)

  @JsonCodec
  case class AlertGroup(labels: Map[String, String],
                        receiver: ReceiverName,
                        alerts: List[Alert])

  @JsonCodec
  case class AlertManagerConfig(original: String)

  @JsonCodec
  case class AlertManagerStatus(cluster: ClusterStatus,
                                config: AlertManagerConfig,
                                uptime: DateTime,
                                versionInfo: VersionInfo)

  @JsonCodec
  case class AlertStatus(inhibitedBy: List[String],
                         silencedBy: List[String],
                         state: String)

  @JsonCodec
  case class ClusterStatus(name: String,
                           peers: List[Peer],
                           status: String)

  @JsonCodec
  case class Labels(alertname: String,
                    labels: Map[String, String])

  @JsonCodec
  case class Matcher(regex: Boolean,
                     name: String,
                     value: String)

  @JsonCodec
  case class Peer(address: String,
                  name: String)

  @JsonCodec
  case class PostableAlert(labels: Map[String, String],
                           annotations: Map[String, String],
                           startsAt: String,
                           endsAt: String,
                           generatorURL: String)

  @JsonCodec
  case class PostableSilence(id: String,
                             comment: String,
                             createdBy: String,
                             endsAt: String,
                             startsAt: String,
                             matchers: List[Matcher])

  @JsonCodec
  case class ReceiverName(name: String)

  @JsonCodec
  case class Silence(id: String,
                     status: SilenceStatus,
                     updatedAt: DateTime,
                     comment: String,
                     createdBy: String,
                     endsAt: DateTime,
                     startsAt: DateTime,
                     matchers: List[Matcher])

  @JsonCodec
  case class SilenceId(silenceID: String)

  @JsonCodec
  case class SilenceStatus(state: String)

  @JsonCodec
  case class VersionInfo(branch: String,
                         buildDate: String,
                         buildUser: String,
                         goVersion: String,
                         revision: String,
                         version: String)
}