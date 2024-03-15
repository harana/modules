package com.harana.designer.backend.modules.projects.models

import com.harana.modules.argo.events.EventSource._
import io.circe.generic.JsonCodec

@JsonCodec
case class Trigger(name: String,
                   calendar: Option[Calendar] = None,
                   file: Option[File] = None,
                   github: Option[Github] = None,
                   gitlab: Option[Gitlab] = None,
                   hdfs: Option[Hdfs] = None,
                   kafka: Option[Kafka] = None,
                   redis: Option[Redis] = None,
                   resource: Option[Resource] = None,
                   slack: Option[Slack] = None,
                   sns: Option[SNS] = None,
                   sqs: Option[SQS] = None,
                   stripe: Option[Stripe] = None,
                   webhook: Option[Webhook] = None)