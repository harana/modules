package com.harana.modules

import play.api.libs.json.{Format, Json}
import skuber.Security.{SELinuxOptions, Sysctl}
import skuber.{LocalObjectReference, PodSecurityContext}

package object argo {

  implicit lazy val affinityFmt: Format[Affinity] = Json.format[Affinity]
  implicit lazy val awsElasticBlockStoreVolumeSourceFmt: Format[AWSElasticBlockStoreVolumeSource] = Json.format[AWSElasticBlockStoreVolumeSource]
  implicit lazy val backoffFmt: Format[Backoff] = Json.format[Backoff]
  implicit lazy val basicAuthFmt: Format[BasicAuth] = Json.format[BasicAuth]
  implicit lazy val configMapKeySelectorFmt: Format[ConfigMapKeySelector] = Json.format[ConfigMapKeySelector]
  implicit lazy val configMapVolumeSourceFmt: Format[ConfigMapVolumeSource] = Json.format[ConfigMapVolumeSource]
  implicit lazy val containerFmt: Format[Container] = Json.format[Container]
  implicit lazy val environmentVariableFmt: Format[EnvironmentVariable] = Json.format[EnvironmentVariable]
  implicit lazy val fieldsV1Fmt: Format[FieldsV1] = Json.format[FieldsV1]
  implicit lazy val hostPathVolumeSourceFmt: Format[HostPathVolumeSource] = Json.format[HostPathVolumeSource]
  implicit lazy val keyToPathFmt: Format[KeyToPath] = Json.format[KeyToPath]
  implicit lazy val k8sResourcePolicyFmt: Format[K8SResourcePolicy] = Json.format[K8SResourcePolicy]
  implicit lazy val labelSelectorRequirementFmt: Format[LabelSelectorRequirement] = Json.format[LabelSelectorRequirement]
  implicit lazy val labelSelectorFmt: Format[LabelSelector] = Json.format[LabelSelector]
  implicit lazy val localObjectReferenceFmt: Format[LocalObjectReference] = Json.format[LocalObjectReference]
  implicit lazy val managedFieldsEntryFmt: Format[ManagedFieldsEntry] = Json.format[ManagedFieldsEntry]
  implicit lazy val microTimeFmt: Format[MicroTime] = Json.format[MicroTime]
  implicit lazy val nfsVolumeSourceFmt: Format[NFSVolumeSource] = Json.format[NFSVolumeSource]
  implicit lazy val nodeAffinityFmt: Format[NodeAffinity] = Json.format[NodeAffinity]
  implicit lazy val nodeStatusFmt: Format[NodeStatus] = Json.format[NodeStatus]
  implicit lazy val nodeSelectorRequirementFmt: Format[NodeSelectorRequirement] = Json.format[NodeSelectorRequirement]
  implicit lazy val nodeSelectorTermFmt: Format[NodeSelectorTerm] = Json.format[NodeSelectorTerm]
  implicit lazy val objectMetaFmt: Format[ObjectMetadata] = Json.format[ObjectMetadata]
  implicit lazy val nodeSelectorFmt: Format[NodeSelector] = Json.format[NodeSelector]
  implicit lazy val objectResourceFmt: Format[ObjectResource] = Json.format[ObjectResource]
  implicit lazy val ownerReferenceFmt: Format[OwnerReference] = Json.format[OwnerReference]
  implicit lazy val parameterFmt: Format[Parameter] = Json.format[Parameter]
  implicit lazy val payloadFieldFmt: Format[PayloadField] = Json.format[PayloadField]
  implicit lazy val persistentVolumeClaimFmt: Format[PersistentVolumeClaim] = Json.format[PersistentVolumeClaim]
  implicit lazy val persistentVolumeClaimVolumeSourceFmt: Format[PersistentVolumeClaimVolumeSource] = Json.format[PersistentVolumeClaimVolumeSource]
  implicit lazy val podAffinityFmt: Format[PodAffinity] = Json.format[PodAffinity]
  implicit lazy val podAffinityTermFmt: Format[PodAffinityTerm] = Json.format[PodAffinityTerm]
  implicit lazy val podAntiAffinityFmt: Format[PodAntiAffinity] = Json.format[PodAntiAffinity]
  implicit lazy val podSecurityContextFmt: Format[PodSecurityContext] = Json.format[PodSecurityContext]
  implicit lazy val preferredSchedulingTermFmt: Format[PreferredSchedulingTerm] = Json.format[PreferredSchedulingTerm]
  implicit lazy val rateLimitFmt: Format[RateLimit] = Json.format[RateLimit]
  implicit lazy val requestsFmt: Format[Requests] = Json.format[Requests]
  implicit lazy val resourcesFmt: Format[Resources] = Json.format[Resources]
  implicit lazy val secretKeySelectorFmt: Format[SecretKeySelector] = Json.format[SecretKeySelector]
  implicit lazy val secureHeaderFmt: Format[SecureHeader] = Json.format[SecureHeader]
  implicit lazy val seLinuxOptionsFmt: Format[SELinuxOptions] = Json.format[SELinuxOptions]
  implicit lazy val serviceFmt: Format[Service] = Json.format[Service]
  implicit lazy val servicePortFmt: Format[ServicePort] = Json.format[ServicePort]
  implicit lazy val sysctlFmt: Format[Sysctl] = Json.format[Sysctl]
  implicit lazy val templateFmt: Format[Template] = Json.format[Template]
  implicit lazy val templateMetdataFmt: Format[TemplateMetadata] = Json.format[TemplateMetadata]
  implicit lazy val timeFmt: Format[Time] = Json.format[Time]
  implicit lazy val tlsConfigFmt: Format[TLSConfig] = Json.format[TLSConfig]
  implicit lazy val valueFromFmt: Format[ValueFrom] = Json.format[ValueFrom]
  implicit lazy val valueFromSourceFmt: Format[ValueFromSource] = Json.format[ValueFromSource]
  implicit lazy val volumeFmt: Format[Volume] = Json.format[Volume]
  implicit lazy val volumeMountFmt: Format[VolumeMount] = Json.format[VolumeMount]
  implicit lazy val volumeSourceFmt: Format[VolumeSource] = Json.format[VolumeSource]
  implicit lazy val weightedPodAffinityTermFmt: Format[WeightedPodAffinityTerm] = Json.format[WeightedPodAffinityTerm]


  case class Affinity(nodeAffinity: Option[NodeAffinity] = None,
                      podAffinity: Option[PodAffinity] = None,
                      podAntiAffinity: Option[PodAntiAffinity] = None)

  case class AWSElasticBlockStoreVolumeSource(fsType: Option[String] = None,
                                              partition: Option[Int] = None,
                                              readOnly: Option[Boolean] = None,
                                              volumeID: Option[String] = None)

  case class BasicAuth(username: SecretKeySelector,
                       password: SecretKeySelector)

  case class Backoff(duration: String,
                     factor: Int,
                     jitte: Int,
                     steps: Int)

  type Comparator = String

  case class ConfigMapKeySelector(key: Option[String] = None,
                                  name: Option[String] = None,
                                  optional: Option[Boolean] = None)

  case class ConfigMapVolumeSource(defaultMode: Option[Int] = None,
                                   items: List[KeyToPath] = List())


  case class Container(args: List[String] = List(),
                       command: List[String] = List(),
                       env: List[EnvironmentVariable] = List(),
                       image: String,
                       imagePullPolicy: Option[String] = None,
                       mirrorVolumeMounts: Option[Boolean] = None,
                       name: String,
                       resources: Option[Resources] = None,
                       volumeMounts: List[VolumeMount] = List())

  case class EnvironmentVariable(name: String,
                                 value: String)

  case class FieldsV1(Raw: Option[String] = None)

  case class HostPathVolumeSource(path: Option[String] = None,
                                  `type`: Option[String] = None)

  type JSONType = String

  case class K8SResourcePolicy(labels: Map[String, String],
                               backoff: Backoff,
                               errorOnBackoffTimeout: Boolean)

  case class KeyToPath(key: Option[String] = None,
                       mode: Option[Int] = None,
                       path: Option[String] = None)

  case class LabelSelector(matchExpressions: List[LabelSelectorRequirement] = List(),
                           matchLabels: Map[String, String] = Map())

  case class LabelSelectorRequirement(key: Option[String] = None,
                                      operator: Option[String] = None,
                                      values: List[String] = List())

  case class ManagedFieldsEntry(apiVersion: Option[String] = None,
                                fieldsType: Option[String] = None,
                                fieldsV1: Option[FieldsV1] = None,
                                manager: Option[String] = None,
                                action: Option[String] = None,
                                time: Option[Time] = None)

  case class MicroTime(Time: String)

  case class NFSVolumeSource(path: Option[String] = None,
                             readOnly: Option[Boolean] = None,
                             server: Option[String] = None)

  case class NodeAffinity(preferredDuringSchedulingIgnoredDuringExecution: List[PreferredSchedulingTerm] = List(),
                          requiredDuringSchedulingIgnoredDuringExecution: Option[NodeSelector] = None)

  case class NodeSelector(nodeSelectorTerms: List[NodeSelectorTerm] = List())

  case class NodeSelectorRequirement(key: Option[String] = None,
                                     operator: Option[String] = None,
                                     values: List[String] = List())

  case class NodeSelectorTerm(matchExpressions: List[NodeSelectorRequirement] = List(),
                              matchFields: List[NodeSelectorRequirement] = List())

  case class NodeStatus(displayName: String,
                        id: String,
                        message: Option[String] = None,
                        name: String,
                        phase: Option[String] = None,
                        updateTime: Option[MicroTime] = None)

  case class ObjectMetadata(annotations: Map[String, String] = Map(),
                            clusterName: Option[String] = None,
                            creationTimestamp: Option[Time] = None,
                            deletionGracePeriodSeconds: Option[Int] = None,
                            deletionTimestamp: Option[Time] = None,
                            finalizers: List[String] = List(),
                            generateName: Option[String] = None,
                            generation: Option[String] = None,
                            labels: Map[String, String] = Map(),
                            managedFields: List[ManagedFieldsEntry] = List(),
                            name: Option[String] = None,
                            namespace: Option[String] = None,
                            ownerReferences: List[OwnerReference] = List(),
                            resourceVersion: Option[String] = None,
                            selfLink: Option[String] = None,
                            uid: Option[String] = None)

  case class ObjectResource(deployment: ObjectMetadata,
                            service: ObjectMetadata)

  case class OwnerReference(apiVersion: Option[String] = None,
                            blockOwnerDeletion: Option[Boolean] = None,
                            controller: Option[Boolean] = None,
                            kind: Option[String] = None,
                            name: Option[String] = None,
                            uid: Option[String] = None)

  case class Parameter(default: Option[String] = None,
                       globalName: Option[String] = None,
                       name: String,
                       value: Option[String] = None,
                       valueFrom: Option[ValueFrom] = None)

  case class PayloadField(path: String,
                          name: String)

  case class PodAffinity(preferredDuringSchedulingIgnoredDuringExecution: List[WeightedPodAffinityTerm] = List(),
                         requiredDuringSchedulingIgnoredDuringExecution: List[PodAffinityTerm] = List())

  case class PodAffinityTerm(labelSelector: Option[LabelSelector] = None,
                             namespaces: List[String] = List(),
                             topologyKey: Option[String] = None)

  case class PodAntiAffinity(labelSelector: Option[LabelSelector] = None,
                             namespaces: List[String] = List(),
                             topologyKey: Option[String] = None)

  case class PersistentVolumeClaim(claimName: String)

  case class PersistentVolumeClaimVolumeSource(claimName: Option[String] = None,
                                               readOnly: Option[Boolean] = None)

  case class PreferredSchedulingTerm(preference: Option[NodeSelectorTerm] = None,
                                     weight: Option[Int] = None)

  type RateLimitUnit = String

  case class RateLimit(unit: RateLimitUnit,
                       requestsPerUnit: Int)

  case class Requests(cpu: Option[String] = None,
                      memory: Option[String] = None)

  case class Resources(requests: Option[Requests] = None)

  case class SecureHeader(name: String,
                          valueFrom: ValueFromSource)

  case class SecretKeySelector(key: Option[String] = None,
                               name: Option[String] = None,
                               optional: Option[Boolean] = None)

  case class Service(ports: List[ServicePort],
                     clusterIP: Option[String] = None)

  case class ServicePort(protocol: String,
                         port: Int,
                         targetPort: Int)

  case class StatusPolicy(allow: List[Int])

  case class Template(metadata: Option[TemplateMetadata] = None,
                      serviceAccountName: Option[String] = None,
                      container: Option[Container] = None,
                      volumes: List[Volume] = List(),
                      securityContext: Option[PodSecurityContext] = None,
                      nodeSelector: Map[String, String] = Map(),
                      imagePullSecrets: List[LocalObjectReference] = List(),
                      priorityClassName: Option[String] = None,
                      priority: Option[Int] = None,
                      affinity: Option[Affinity] = None)

  case class TemplateMetadata(name: Option[String] = None,
                              labels: Map[String, String] = Map(),
                              volumes: List[Volume] = List())

  case class TLSConfig(caCertSecret: SecretKeySelector,
                       clientCertSecret: SecretKeySelector,
                       clientKeySecret: SecretKeySelector)

  case class Time(nanos: Int,
                  seconds: Long)

  case class ValueFrom(path: String)

  case class ValueFromSource(configMapKeyRef: Option[ConfigMapKeySelector] = None,
                             secretKeyRef: Option[SecretKeySelector] = None)

  case class Volume(name: String,
                    persistentVolumeClaim: Option[PersistentVolumeClaim] = None,
                    secret: Map[String, String] = Map())

  case class VolumeMount(mountPath: Option[String] = None,
                         mountPropagation: Option[String] = None,
                         name: String,
                         readOnly: Option[Boolean] = None,
                         subPath: Option[String] = None,
                         subPathExpr: Option[String] = None)

  case class VolumeSource(awsElasticBlockStore: Option[AWSElasticBlockStoreVolumeSource] = None,
                          configMap: Option[ConfigMapVolumeSource] = None,
                          hostPath: Option[HostPathVolumeSource] = None,
                          nfs: Option[NFSVolumeSource] = None,
                          persistentVolumeClaim: Option[PersistentVolumeClaimVolumeSource] = None)

  case class WeightedPodAffinityTerm(podAffinityTerm: Option[PodAffinityTerm] = None,
                                     weight: Option[Int] = None)
}