package com.harana.modules.argo.events

import com.harana.modules.argo._
import com.harana.modules.argo.events.Sensor.{K8SResource, TriggerParameter}
import play.api.libs.json.{Format, Json}

object Trigger {

  implicit lazy val httpTriggerFmt: Format[HttpTrigger] = Json.format[HttpTrigger]
  implicit lazy val k8sTriggerFmt: Format[K8STrigger] = Json.format[K8STrigger]
  implicit lazy val k8sSourceFmt: Format[K8SSource] = Json.format[K8SSource]
  implicit lazy val slackTriggerFmt: Format[SlackTrigger] = Json.format[SlackTrigger]

  case class K8STrigger(group: String,
                        version: String,
                        resource: String,
                        action: String,
                        source: K8SSource,
                        parameters: List[Parameter] = List())

  case class K8SSource(resource: K8SResource)

  case class HttpTrigger(url: String,
                         payload: List[TriggerParameter] = List(),
                         tls: Option[TLSConfig] = None,
                         method: Option[String] = None,
                         parameters: List[TriggerParameter] = List(),
                         timeout: Option[Int] = None,
                         basicAuth: Option[BasicAuth] = None,
                         headers: Map[String, String] = Map(),
                         secureHeaders: List[SecureHeader] = List())

  case class SlackTrigger(slackToken: SecretKeySelector,
                          channel: Option[String] = None,
                          message: Option[String] = None,
                          parameters: List[TriggerParameter] = List())
}