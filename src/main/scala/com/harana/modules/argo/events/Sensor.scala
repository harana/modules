package com.harana.modules.argo.events

import com.harana.modules.argo._
import com.harana.modules.argo.events.Trigger.{HttpTrigger, K8STrigger, SlackTrigger}
import com.harana.modules.argo.workflows.Workflow
import play.api.libs.json.{Format, Json}
import skuber.apiextensions.v1.CustomResourceDefinition
import skuber.{CustomResource, ListResource, ResourceDefinition}

object Sensor {

  type Sensor = CustomResource[Spec, Status]
  type SensorList = ListResource[Sensor]

  implicit lazy val conditionsResetCriteriaFmt: Format[ConditionsResetCriteria] = Json.format[ConditionsResetCriteria]
  implicit lazy val conditionsResetByTimeFmt: Format[ConditionsResetByTime] = Json.format[ConditionsResetByTime]
  implicit lazy val dataFilterFmt: Format[DataFilter] = Json.format[DataFilter]
  implicit lazy val eventContextFmt: Format[EventContext] = Json.format[EventContext]
  implicit lazy val eventDependencyFilterFmt: Format[EventDependencyFilter] = Json.format[EventDependencyFilter]
  implicit lazy val eventDependencyFmt: Format[EventDependency] = Json.format[EventDependency]
  implicit lazy val exprFilterFmt: Format[ExprFilter] = Json.format[ExprFilter]
  implicit lazy val httpFmt: Format[Http] = Json.format[Http]
  implicit lazy val k8sResourceFmt: Format[K8SResource] = Json.format[K8SResource]
  implicit lazy val parameterFmt: Format[Parameter] = Json.format[Parameter]
  implicit lazy val parameterSourceFmt: Format[ParameterSource] = Json.format[ParameterSource]
  implicit lazy val resourceLabelsPolicyFmt: Format[ResourceLabelsPolicy] = Json.format[ResourceLabelsPolicy]
  implicit lazy val specFmt: Format[Spec] = Json.format[Spec]
  implicit lazy val statusFmt: Format[Status] = Json.format[Status]
  implicit lazy val statusPolicyFmt: Format[StatusPolicy] = Json.format[StatusPolicy]
  implicit lazy val subscriptionFmt: Format[Subscription] = Json.format[Subscription]
  implicit lazy val timeFmt: Format[Time] = Json.format[Time]
  implicit lazy val timeFilterFmt: Format[TimeFilter] = Json.format[TimeFilter]
  implicit lazy val triggerTemplateFmt: Format[TriggerTemplate] = Json.format[TriggerTemplate]
  implicit lazy val triggerParameterFmt: Format[TriggerParameter] = Json.format[TriggerParameter]
  implicit lazy val triggerParameterSourceFmt: Format[TriggerParameterSource] = Json.format[TriggerParameterSource]
  implicit lazy val triggerPolicyFmt: Format[TriggerPolicy] = Json.format[TriggerPolicy]
  implicit lazy val triggerFmt: Format[Trigger] = Json.format[Trigger]

  implicit lazy val resourceDefinition: ResourceDefinition[Sensor] = ResourceDefinition[Sensor]("Sensor", "argoproj.io", "v1alpha1")
  val crd = CustomResourceDefinition[Sensor]

  def apply(name: String, spec: Spec) = CustomResource[Spec, Status](spec)
    .withLabels(("sensors.argoproj.io/sensor-controller-instanceid", "argo"))
    .withName(name)

  case class ConditionsResetCriteria(byTime: ConditionsResetByTime)

  case class ConditionsResetByTime(cron: String,
                                   timezone: Option[String] = None)

  case class DataFilter(path: String,
                        `type`: JSONType,
                        value: List[String] = List(),
                        comparator: Option[Comparator] = None,
                        template: Option[String] = None)

  case class EventContext(id: String,
                          source: String,
                          specversion: String,
                          `type`: String,
                          datacontenttype: String,
                          subject: String,
                          time: Time)

  case class EventDependencyFilter(time: Option[TimeFilter] = None,
                                   context: Option[EventContext] = None,
                                   data: List[DataFilter] = List(),
                                   exprs: List[ExprFilter] = List())

  case class EventDependency(name: String,
                             eventSourceName: String,
                             eventName: String,
                             filters: Option[EventDependencyFilter] = None)

  case class ExprFilter(expr: String,
                        fields: List[PayloadField])

  case class Http(port: Int)

  case class K8SResource(apiVersion: String,
                         kind: String,
                         metadata: ObjectMetadata,
                         spec: Workflow.Spec)

  case class Parameter(dest: String,
                       action: Option[String] = None,
                       src: ParameterSource)

  case class ParameterSource(contextKey: Option[String] = None,
                             dataKey: Option[String] = None,
                             event: String,
                             value: Option[String] = None)

  case class Policy(backoff: Backoff,
                    errorOnBackoffTimeout: Boolean,
                    resourceLabels: ResourceLabelsPolicy)

  case class ResourceLabelsPolicy(labels: String)

  case class Spec(template: Option[Template] = None,
                  dependencies: List[EventDependency] = List(),
                  errorOnFailedRound: Option[Boolean] = None,
                  eventBusName: Option[String] = None,
                  replicas: Option[Int] = None,
                  subscription: Option[Subscription] = None,
                  triggers: List[Trigger] = List())

  case class Status(completedAt: Option[Time] = None,
                    lastCycleTime: Time,
                    message: Option[String] = None,
                    nodes: Option[NodeStatus] = None,
                    phase: String,
                    resources: ObjectResource,
                    startedAt: Option[Time] = None,
                    triggerCycleCount: Option[Int] = None,
                    triggerCycleStatus: Int)

  case class Subscription(http: Option[Http] = None)

  case class TimeFilter(start: String,
                        stop: String)

  case class Trigger(template: TriggerTemplate,
                     parameters: List[TriggerParameter] = List(),
                     policy: Option[TriggerPolicy] = None,
                     retryStrategy: Option[Backoff] = None,
                     rateLimit: Option[RateLimit] = None)

  case class TriggerTemplate(name: String,
                             conditions: Option[String] = None,
                             http: Option[HttpTrigger] = None,
                             k8s: Option[K8STrigger] = None,
                             slack: Option[SlackTrigger] = None,
                             conditionsReset: List[ConditionsResetCriteria] = List())


  case class TriggerParameter(src: TriggerParameterSource,
                              dest: String,
                              action: TriggerParameterOption)

  case class TriggerParameterSource(dependencyName: String,
                                    contextKey: String,
                                    contextTemplate: String,
                                    dataKey: String,
                                    dataTemplate: String,
                                    value: String)

  type TriggerParameterOption = String

  case class TriggerPolicy(k8s: Option[K8SResourcePolicy] = None,
                           status: Option[StatusPolicy] = None)
}