package com.harana.modules.argo.workflows

import ai.x.play.json.Jsonx
import com.harana.modules.argo.{Affinity, NodeStatus, PersistentVolumeClaim, SecretKeySelector, Time}
import play.api.libs.json.{Format, Json}
import skuber.apiextensions.CustomResourceDefinition
import skuber.{CustomResource, ListResource, ResourceDefinition}

object Workflow {

  type Workflow = CustomResource[Spec, Status]
  type WorkflowList = ListResource[Workflow]

  implicit lazy val specFmt: Format[Spec] = Jsonx.formatCaseClass[Spec]
  implicit lazy val statusFmt: Format[Status] = Json.format[Status]
  implicit lazy val resourceDefinition: ResourceDefinition[Workflow] = ResourceDefinition[Workflow]("Workflow", "argoproj.io", "v1alpha1")
  val crd = CustomResourceDefinition[Workflow]

  def apply(name: String, spec: Spec) = CustomResource[Spec, Status](spec).withName(name)

  case class Spec(activeDeadlineSeconds: Option[Int] = None,
                  affinity: Option[Affinity] = None,
                  arguments: Option[Arguments] = None,
                  automountServiceAccountToken: Option[Boolean] = None,
                  entrypoint: Option[String] = None,
                  hostNetwork: Option[Boolean] = None,
                  imagePullSecrets: List[SecretKeySelector] = List(),
                  nodeSelector: Option[String] = None,
                  onExit: Option[String] = None,
                  parallelism: Option[Int] = None,
                  podGC: Option[PodGC] = None,
                  podSpecPath: Option[String] = None,
                  priority: Option[Int] = None,
                  schedulerName: Option[String] = None,
                  serviceAccountName: Option[String] = None,
                  suspend: Option[Boolean] = None,
                  templates: List[Template] = List(),
                  tolerations: List[Toleration] = List(),
                  ttlSecondsAfterFinished: Option[Int] = None,
                  ttlStrategy: Option[TtlStrategy] = None,
                  volumeClaimTemplates: List[PersistentVolumeClaim] = List(),
                  volumes: List[Volume] = List())

  case class Status(compressedNodes: String,
                    finishedAt: Time,
                    message: String,
                    nodes: NodeStatus,
                    offloadNodeStatusVersion: String,
                    outputs: Outputs,
                    persistentVolumeClaims: List[Volume] = List(),
                    phase: String,
                    startedAt: Time,
                    storedTemplates: Template)

}