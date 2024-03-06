package com.harana.modules.argo

import ai.x.play.json.{CamelToSnakeNameEncoder, Jsonx}
import play.api.libs.json.{Format, Json}

package object workflows {

  implicit lazy val encoder: CamelToSnakeNameEncoder = CamelToSnakeNameEncoder()

  implicit lazy val argumentsFmt: Format[Arguments] = Json.format[Arguments]
  implicit lazy val artifactoryFmt: Format[Artifactory] = Json.format[Artifactory]
  implicit lazy val artifactoryArtifactFmt: Format[ArtifactoryArtifact] = Json.format[ArtifactoryArtifact]
  implicit lazy val artifactoryAuthFmt: Format[ArtifactoryAuth] = Json.format[ArtifactoryAuth]
  implicit lazy val artifactFmt: Format[Artifact] = Json.format[Artifact]
  implicit lazy val artifactLocationFmt: Format[ArtifactLocation] = Json.format[ArtifactLocation]
  implicit lazy val backoffFmt: Format[Backoff] = Json.format[Backoff]
  implicit lazy val continueOnFmt: Format[ContinueOn] = Json.format[ContinueOn]
  implicit lazy val dagFmt: Format[DAG] = Json.format[DAG]
  implicit lazy val dagTaskFmt: Format[DAGTask] = Json.format[DAGTask]
  implicit lazy val executorConfigFmt: Format[ExecutorConfig] = Json.format[ExecutorConfig]
  implicit lazy val gitArtifactFmt: Format[GitArtifact] = Json.format[GitArtifact]
  implicit lazy val hdfsArtifactFmt: Format[HDFSArtifact] = Json.format[HDFSArtifact]
  implicit lazy val hdfsConfigFmt: Format[HDFSConfig] = Json.format[HDFSConfig]
  implicit lazy val hdfsKrbConfigFmt: Format[HDFSKrbConfig] = Json.format[HDFSKrbConfig]
  implicit lazy val httpArtifactFmt: Format[HTTPArtifact] = Json.format[HTTPArtifact]
  implicit lazy val inputsFmt: Format[Inputs] = Json.format[Inputs]
  implicit lazy val metadataFmt: Format[Metadata] = Json.format[Metadata]
  implicit lazy val outputsFmt: Format[Outputs] = Json.format[Outputs]
  implicit lazy val podGcFmt: Format[PodGC] = Json.format[PodGC]
  implicit lazy val rawFmt: Format[Raw] = Json.format[Raw]
  implicit lazy val rawArtifactFmt: Format[RawArtifact] = Json.format[RawArtifact]
  implicit lazy val resourceFmt: Format[Resource] = Json.format[Resource]
  implicit lazy val retryStrategyFmt: Format[RetryStrategy] = Json.format[RetryStrategy]
  implicit lazy val s3Fmt: Format[S3] = Json.format[S3]
  implicit lazy val s3ArtifactFmt: Format[S3Artifact] = Json.format[S3Artifact]
  implicit lazy val s3BucketFmt: Format[S3Bucket] = Json.format[S3Bucket]
  implicit lazy val scriptFmt: Format[Script] = Json.format[Script]
  implicit lazy val sequenceFmt: Format[Sequence] = Json.format[Sequence]
  implicit lazy val sidecarFmt: Format[Sidecar] = Json.format[Sidecar]
  implicit lazy val stepFmt: Format[Step] = Json.format[Step]
  implicit lazy val suspendFmt: Format[Suspend] = Json.format[Suspend]
  implicit lazy val templateFmt: Format[Template] = Jsonx.formatCaseClass[Template]
  implicit lazy val templateRefFmt: Format[TemplateRef] = Json.format[TemplateRef]
  implicit lazy val tolerationFmt: Format[Toleration] = Json.format[Toleration]
  implicit lazy val ttlStrategyFmt: Format[TtlStrategy] = Json.format[TtlStrategy]
  implicit lazy val volumeFmt: Format[Volume] = Json.format[Volume]

  case class Arguments(artifacts: List[Artifact] = List(),
                       parameters: List[Parameter] = List())

  case class Artifactory(url: Option[String] = None,
                         usernameSecret: Option[SecretKeySelector] = None,
                         passwordSecret: Option[SecretKeySelector] = None)

  case class ArtifactoryArtifact(artifactoryAuth: Option[ArtifactoryAuth] = None,
                                 url: Option[String] = None)

  case class ArtifactoryAuth(passwordSecret: Option[SecretKeySelector] = None,
                             usernameSecret: Option[SecretKeySelector] = None)

  case class Artifact(name: String,
                      artifactory: Option[Artifactory] = None,
                      http: Map[String, String] = Map(),
                      path: Option[String] = None,
                      raw: Option[Raw] = None,
                      s3: Option[S3] = None,
                      mode: Option[String] = None)

  case class ArtifactLocation(archiveLogs: Option[Boolean] = None,
                              artifactory: Option[ArtifactoryArtifact] = None,
                              git: Option[GitArtifact] = None,
                              hdfs: Option[HDFSArtifact] = None,
                              http: Option[HTTPArtifact] = None,
                              raw: Option[RawArtifact] = None,
                              s3: Option[S3Artifact] = None)

  case class Backoff(duration: Option[String] = None,
                     factor: Option[Int] = None,
                     maxDuration: Option[String] = None)

  case class ContinueOn(error: Option[Boolean] = None,
                        failed: Option[Boolean] = None)

  case class DAG(failFast: Option[Boolean] = None,
                 target: Option[String] = None,
                 tasks: List[DAGTask] = List())

  case class DAGTask(arguments: Option[Arguments] = None,
                     continueOn: Option[ContinueOn] = None,
                     dependencies: List[String] = List(),
                     name: Option[String] = None,
                     onExit: Option[String] = None,
                     template: Option[String] = None,
                     templateRef: Option[TemplateRef] = None,
                     when: Option[String] = None,
                     withItems: List[String] = List(),
                     withSequence: Option[Sequence] = None)

  case class ExecutorConfig(serviceAccountName: Option[String] = None)

  case class GitArtifact(depth: Option[Long] = None,
                         fetch: List[String] = List())

  case class HDFSArtifact(force: Option[Boolean] = None,
                          hDFSConfig: Option[HDFSConfig] = None,
                          path: Option[String] = None)

  case class HDFSConfig(hDFSKrbConfig: Option[HDFSKrbConfig] = None,
                        hdfsUser: Option[String] = None)

  case class HDFSKrbConfig(krbCCacheSecret: Option[SecretKeySelector] = None,
                           krbConfigConfigMap: Option[ConfigMapKeySelector] = None,
                           krbKeytabSecret: Option[SecretKeySelector] = None,
                           krbRealm: Option[String] = None,
                           krbServicePrincipalName: Option[String] = None,
                           krbUsername: Option[String] = None)

  case class HTTPArtifact(url: Option[String] = None)

  case class Inputs(artifacts: List[Artifact] = List(),
                    parameters: List[Parameter] = List())

  case class Metadata(annotations: Option[String] = None,
                      labels: Option[String] = None)

  case class Outputs(artifacts: List[Artifact] = List(),
                     parameters: List[Parameter] = List(),
                     result: Option[String] = None)

  case class PodGC(strategy: String)

  case class Raw(data: String)

  case class RawArtifact(data: Option[String] = None)

  case class Resource(action: String,
                      failureCondition: Option[String] = None,
                      manifest: Option[String] = None,
                      successCondition: Option[String] = None)

  case class RetryStrategy(backoff: Option[Backoff] = None,
                           limit: Int,
                           retryPolicy: String)

  case class S3(accessKeySecret: Option[SecretKeySelector] = None,
                bucket: Option[String] = None,
                endpoint: Option[String] = None,
                insecure: Option[Boolean] = None,
                key: Option[String] = None,
                region: Option[String] = None,
                roleARN: Option[String] = None,
                secretKeySecret: Option[SecretKeySelector] = None)

  case class S3Artifact(key: Option[String] = None,
                        s3bucket: Option[S3Bucket] = None)

  case class S3Bucket(accessKeySecret: Option[SecretKeySelector] = None,
                      bucket: Option[String] = None,
                      endpoint: Option[String] = None,
                      insecure: Option[Boolean] = None,
                      region: Option[Boolean] = None,
                      roleARN: Option[String] = None,
                      secretKeySecret: Option[SecretKeySelector])

  case class Script(image: String,
                    command: List[String] = List(),
                    source: String)

  case class Sidecar(name: String,
                     image: String)

  case class Sequence(count: Option[String] = None,
                      end: Option[String] = None,
                      format: Option[String] = None,
                      start: Option[String] = None)

  case class Step(arguments: Option[Arguments] = None,
                  continueOn: Option[ContinueOn] = None,
                  name: String,
                  onExit: Option[String] = None,
                  template: Option[String] = None,
                  templateRef: Option[String] = None,
                  when: Option[String] = None,
                  withItems: List[String] = List(),
                  withParam: Option[String] = None,
                  withSequence: Option[Sequence] = None)

  case class Suspend(duration: Option[String] = None)

  case class Template(archiveLocation: Option[ArtifactLocation] = None,
                      container: Option[Container] = None,
                      daemon: Option[Boolean] = None,
                      dag: Option[DAG] = None,
                      executor: Option[ExecutorConfig] = None,
                      initContainers: List[Container] = List(),
                      inputs: Option[Inputs] = None,
                      metadata: Option[Metadata] = None,
                      name: String,
                      nodeSelector: Option[String] = None,
                      outputs: Option[Outputs] = None,
                      parallelism: Option[Int] = None,
                      parameters: List[Parameter] = List(),
                      podSpecPath: Option[String] = None,
                      priority: Option[Int] = None,
                      priorityClassName: Option[String] = None,
                      resource: Option[Resource] = None,
                      retryStrategy: Option[RetryStrategy] = None,
                      schedulerName: Option[String] = None,
                      script: Option[Script] = None,
                      serviceAccountName: Option[String] = None,
                      sidecars: List[Container] = List(),
                      steps: List[Step] = List(),
                      suspend: Option[Suspend] = None,
                      template: Option[String] = None,
                      templateRef: Option[TemplateRef] = None,
                      tolerations: List[Toleration] = List(),
                      volumes: List[Volume] = List())

  case class TemplateRef(name: Option[String] = None,
                         runtimeResolution: Option[Boolean] = None,
                         template: Option[String] = None)

  case class Toleration(effect: Option[String] = None,
                        key: Option[String] = None,
                        operator: Option[String] = None,
                        tolerationSeconds: Option[Int] = None,
                        value: Option[String] = None)

  case class TtlStrategy(secondsAfterCompletion: Option[Int] = None,
                         secondsAfterSuccess: Option[Int] = None,
                         secondsAfterFailure: Option[Int] = None)

  case class Volume(name: Option[String] = None,
                    volumeSource: Option[VolumeSource] = None)
}
