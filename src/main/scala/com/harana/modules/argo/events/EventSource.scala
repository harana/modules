package com.harana.modules.argo.events

import com.harana.modules.argo._
import io.circe.generic.JsonCodec
import play.api.libs.json.{Format, Json}
import skuber.apiextensions.v1.CustomResourceDefinition
import skuber.{CustomResource, ListResource, ResourceDefinition}

object EventSource {

  type EventSource = CustomResource[Spec, Status]
  type EventSourceList = ListResource[EventSource]

  implicit lazy val calendarFmt: Format[Calendar] = Json.format[Calendar]
  implicit lazy val catchupConfigurationFmt: Format[CatchupConfiguration] = Json.format[CatchupConfiguration]
  implicit lazy val configMapPersistenceFmt: Format[ConfigMapPersistence] = Json.format[ConfigMapPersistence]
  implicit lazy val eventPersistenceFmt: Format[EventPersistence] = Json.format[EventPersistence]
  implicit lazy val eventSourceFilterFmt: Format[EventSourceFilter] = Json.format[EventSourceFilter]
  implicit lazy val fileFmt: Format[File] = Json.format[File]
  implicit lazy val genericFmt: Format[Generic] = Json.format[Generic]
  implicit lazy val githubFmt: Format[Github] = Json.format[Github]
  implicit lazy val gitlabFmt: Format[Gitlab] = Json.format[Gitlab]
  implicit lazy val hdfsFmt: Format[Hdfs] = Json.format[Hdfs]
  implicit lazy val kafkaFmt: Format[Kafka] = Json.format[Kafka]
  implicit lazy val redisFmt: Format[Redis] = Json.format[Redis]
  implicit lazy val resourceFmt: Format[Resource] = Json.format[Resource]
  implicit lazy val slackFmt: Format[Slack] = Json.format[Slack]
  implicit lazy val snsFmt: Format[SNS] = Json.format[SNS]
  implicit lazy val specFmt: Format[Spec] = Json.format[Spec]
  implicit lazy val sqsFmt: Format[SQS] = Json.format[SQS]
  implicit lazy val stripeFmt: Format[Stripe] = Json.format[Stripe]
  implicit lazy val statusFmt: Format[Status] = Json.format[Status]
  implicit lazy val watchPathConfigFmt: Format[WatchPathConfig] = Json.format[WatchPathConfig]
  implicit lazy val webhookFmt: Format[Webhook] = Json.format[Webhook]

  implicit lazy val resourceDefinition: ResourceDefinition[EventSource] = ResourceDefinition[EventSource]("EventSource", "argoproj.io", "v1alpha1")
  val crd = CustomResourceDefinition[EventSource]

  def apply(name: String, spec: Spec) = CustomResource[Spec, Status](spec).withName(name)

  @JsonCodec
  case class Calendar(exclusionDates: List[String],
                      interval: Option[String] = None,
                      schedule: Option[String] = None,
                      timezone: Option[String] = None,
                      metadata: Map[String, String] = Map(),
                      eventPersistence: Option[EventPersistence] = None,
                      filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class CatchupConfiguration(enabled: Boolean,
                                  maxDuration: String)

  @JsonCodec
  case class ConfigMapPersistence(name: String,
                                  createIfNotExist: Boolean)

  @JsonCodec
  case class EventPersistence(catchupConfiguration: CatchupConfiguration,
                              configMapPersistence: ConfigMapPersistence)

  @JsonCodec
  case class EventSourceFilter(expression: String)


  @JsonCodec
  case class File(eventType: String,
                  watchPathConfig: WatchPathConfig,
                  polling: Boolean = false,
                  metadata: Map[String, String] = Map(),
                  filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Generic(url: String,
                     config: Option[String] = None,
                     insecure: Boolean = false,
                     jsonBody: Boolean = true,
                     metadata: Map[String, String] = Map(),
                     authSecret: Option[String] = None,
                     filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Github(owner: String,
                    repository: String,
                    endpoint: String,
                    port: Long,
                    url: Option[String] = None,
                    events: List[String],
                    apiSecret: String,
                    filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Gitlab(projectId: String,
                    endpoint: String,
                    port: Long,
                    url: Option[String] = None,
                    event: String,
                    apiSecret: String,
                    baseUrl: String,
                    filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Hdfs(directory: String,
                  `type`: String,
                  path: String,
                  addresses: List[String],
                  hdfsUser: String,
                  krbCCacheSecret: Option[String] = None,
                  krbKeytabSecret: Option[String] = None,
                  krbUsername: Option[String] = None,
                  krbRealm: Option[String] = None,
                  krbConfigConfigMap: Option[String] = None,
                  krbServicePrincipalName: Option[String] = None,
                  filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Kafka(url: String,
                   topic: String,
                   partition: Int,
                   backOffDuration: Int,
                   backOffSteps: Int,
                   backOffFactor: Int,
                   backOffJitter: Int,
                   filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Redis(hostAddress: String,
                   hostPasswordSecret: Option[String] = None,
                   db: Option[Int] = None,
                   channels: List[String],
                   filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Resource(namespace: String,
                      group: Option[String] = None,
                      version: Option[String] = None,
                      resource: String,
                      `type`: String,
                      filters: Map[String, String],
                      filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Slack(endpoint: String,
                   port: Long,
                   token: String,
                   signingSecret: Option[String] = None,
                   filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class SNS(endpoint: String,
                 port: Long,
                 url: Option[String] = None,
                 accessKeySecret: Option[String] = None,
                 secretKeySecret: Option[String] = None,
                 region: Option[String] = None,
                 filter: Option[EventSourceFilter] = None)

  case class Spec(eventBusName: String,
                  calendar: Map[String, Calendar] = Map(),
                  file: Map[String, File] = Map(),
                  generic: Map[String, Generic] = Map(),
                  github: Map[String, Github] = Map(),
                  gitlab: Map[String, Gitlab] = Map(),
                  hdfs: Map[String, Hdfs] = Map(),
                  kafka: Map[String, Kafka] = Map(),
                  redis: Map[String, Redis] = Map(),
                  replicas: Int = 1,
                  resource: Map[String, Resource] = Map(),
                  service: Option[Service] = None,
                  slack: Map[String, Slack] = Map(),
                  sns: Map[String, SNS] = Map(),
                  sqs: Map[String, SQS] = Map(),
                  stripe: Map[String, Stripe] = Map(),
                  template: Option[Template] = None,
                  webhook: Map[String, Webhook] = Map())

  @JsonCodec
  case class SQS(accessKeySecret: Option[String] = None,
                 secretKeySecret: Option[String] = None,
                 region: Option[String] = None,
                 queue: String,
                 waitTimeSeconds: Option[Int],
                 filter: Option[EventSourceFilter] = None)

  @JsonCodec
  case class Stripe(endpoint: String,
                    port: Long,
                    url: Option[String] = None,
                    apiKeySecret: String,
                    eventFilters: List[String],
                    filter: Option[EventSourceFilter] = None)

  case class Status(createdAt: Time)

  @JsonCodec
  case class WatchPathConfig(directory: Option[String] = None,
                             path: Option[String] = None,
                             pathRegexp: Option[String] = None)

  @JsonCodec
  case class Webhook(endpoint: String,
                     port: Long,
                     filter: Option[EventSourceFilter] = None)
}