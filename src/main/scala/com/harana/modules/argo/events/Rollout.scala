package com.harana.modules.argo.events

import com.harana.modules.argo._
import play.api.libs.json.{Format, Json}
import skuber.apiextensions.CustomResourceDefinition
import skuber.{CustomResource, ListResource, ResourceDefinition}

object Rollout {

  type Rollout = CustomResource[Spec, Status]
  type RolloutList = ListResource[Rollout]

  implicit lazy val analysisFmt: Format[Analysis] = Json.format[Analysis]
  implicit lazy val blueGreenFmt: Format[BlueGreen] = Json.format[BlueGreen]
  implicit lazy val blueGreenStatusFmt: Format[BlueGreenStatus] = Json.format[BlueGreenStatus]
  implicit lazy val canaryFmt: Format[Canary] = Json.format[Canary]
  implicit lazy val canaryStatusFmt: Format[CanaryStatus] = Json.format[CanaryStatus]
  implicit lazy val canaryStepFmt: Format[CanaryStep] = Json.format[CanaryStep]
  implicit lazy val pauseConditionFmt: Format[PauseCondition] = Json.format[PauseCondition]
  implicit lazy val rolloutConditionFmt: Format[RolloutCondition] = Json.format[RolloutCondition]
  implicit lazy val specFmt: Format[Spec] = Json.format[Spec]
  implicit lazy val statusFmt: Format[Status] = Json.format[Status]
  implicit lazy val strategyFmt: Format[Strategy] = Json.format[Strategy]

  implicit lazy val resourceDefinition: ResourceDefinition[Rollout] = ResourceDefinition[Rollout]("Rollout", "argoproj.io", "v1alpha1")
  val crd = CustomResourceDefinition[Rollout]

  def apply(name: String, spec: Spec) = CustomResource[Spec, Status](spec).withName(name)

  case class Analysis(successfulRunHistoryLimit: Option[Int] = None,
                      unsuccessfulRunHistoryLimit: Option[Int] = None)

  case class BlueGreen(activeService: String,
                       previewService: String,
                       previewReplicaCount: Option[Int],
                       autoPromotionEnabled: Option[Boolean],
                       autoPromotionSeconds: Option[Int],
                       scaleDownDelaySeconds: Option[Int],
                       scaleDownDelayRevisionLimit: Option[Int])

  case class BlueGreenStatus(activeSelector: String,
                             previousActiveSelector: String,
                             previewSelector: String,
                             scaleDownDelayStartTime: Time,
                             scaleUpPreviewCheckPoint: Boolean)

  case class Canary(stableService: String,
                    canaryService: String,
                    steps: List[CanaryStep] = List(),
                    maxSurge: Option[String] = None,
                    maxUnavailable: Option[String] = None)

  case class CanaryStatus(currentBackgroundAnalysisRun: String,
                          currentExperiment: String,
                          currentStepAnalysisRun: String,
                          stableRS: String)

  case class CanaryStep(weight: Int,
                        pause: Option[Int])

  case class PauseCondition(reason: Option[String] = None,
                            startTime: Time)

  case class RolloutCondition(lastTransitionTime: Time,
                              lastUpdateTime: Time,
                              message: String,
                              reason: String,
                              status: String,
                              `type`: String)

  case class Spec(analysis: Option[Analysis] = None,
                  minReadySeconds: Option[Int] = None,
                  paused: Boolean = false,
                  progressDeadlineAbort: Boolean = false,
                  progressDeadlineSeconds: Option[Int] = None,
                  replicas: Option[Int] = Some(1),
                  restartAt: Option[String] = None,
                  revisionHistoryLimit: Option[Int] = None,
                  selector: Option[NodeSelector] = None,
                  strategy: Option[Strategy] = None)

  case class Status(abort: Option[Boolean] = None,
                    pauseConditions: List[PauseCondition] = List(),
                    controllerPause: Option[Boolean] = None,
                    currentPodHash: Option[String] = None,
                    replicas: Option[Int] = None,
                    updatedReplicas: Option[Int] = None,
                    readyReplicas: Option[Int] = None,
                    availableReplicas: Option[Int] = None,
                    currentStepIndex: Option[Int] = None,
                    collisionCount: Option[Int] = None,
                    observedGeneration: Option[String] = None,
                    conditions: List[RolloutCondition] = List(),
                    canary: Option[CanaryStatus] = None,
                    blueGreen: Option[BlueGreenStatus] = None,
                    HPAReplicas: Option[Int] = None,
                    selector: Option[String] = None)

  case class Strategy(blueGreen: Option[BlueGreen] = None,
                      canary: Option[Canary] = None)
}