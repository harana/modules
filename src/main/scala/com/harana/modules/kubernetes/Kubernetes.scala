package com.harana.modules.kubernetes

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.libs.json.{Format, Writes}
import skuber._
import skuber.api.client._
import skuber.api.patch.Patch
import skuber.apiextensions.v1.CustomResourceDefinition
import zio.macros.accessible
import zio.stream.ZStream
import zio.{IO, Task}

import scala.concurrent.Promise

@accessible
trait Kubernetes {
    def newClient: IO[K8SException, KubernetesClient]

    def get[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Option[O]]

    def exists[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Boolean]

    def save(client: KubernetesClient, namespace: String, crd: CustomResourceDefinition): IO[K8SException, CustomResourceDefinition]

    def createNamespace(client: KubernetesClient, namespace: String)(implicit lc: LoggingContext): IO[K8SException, Namespace]

    def create[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, O]

    def createPodAndWait(client: KubernetesClient, namespace: String, obj: Pod, startupTime: Long)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Pod]

    def podInState(client: KubernetesClient, namespace: String, name: String, desiredState: String)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Boolean]

    def podTerminating(client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Boolean]

    def waitForPodToTerminate(client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Unit]

    def update[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, O]

    def delete[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, gracePeriodSeconds: Int = -1)(implicit rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Unit]

    def deleteWithOptions[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, options: DeleteOptions)(implicit rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Unit]

    def deleteAll[O <: ObjectResource](client: KubernetesClient, namespace: String)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]]

    def deleteAllSelected[O <: ObjectResource](client: KubernetesClient, namespace: String, labelSelector: LabelSelector)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]]

    def getNamespaceNames(client: KubernetesClient)(implicit lc: LoggingContext): IO[K8SException, List[String]]

    def listByNamespace[O <: ObjectResource](client: KubernetesClient)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, Map[String, ListResource[O]]]

    def list[O <: ObjectResource](client: KubernetesClient, namespace: String)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]]

    def listSelected[O <: ObjectResource](client: KubernetesClient, namespace: String, labelSelector: LabelSelector)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]]

    def listWithOptions[O <: ObjectResource](client: KubernetesClient, namespace: String, options: ListOptions)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]]

    def updateStatus[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], statusEv: HasStatusSubresource[O], lc: LoggingContext): IO[K8SException, O]

    def getStatus[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[O], rd: ResourceDefinition[O], statusEv: HasStatusSubresource[O], lc: LoggingContext): IO[K8SException, O]

    def watch[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def watch[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(
      implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def watchAll[O <: ObjectResource](client: KubernetesClient, namespace: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(
      implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def watchContinuously[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def watchContinuously[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(
      implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def watchAllContinuously[O <: ObjectResource](client: KubernetesClient, namespace: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(
      implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def watchWithOptions[O <: ObjectResource](client: KubernetesClient, namespace: String, options: ListOptions, bufsize: Int = 10000)(
      implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]]

    def getScale[O <: ObjectResource](client: KubernetesClient, namespace: String, objName: String)(implicit rd: ResourceDefinition[O], sc: Scale.SubresourceSpec[O], lc: LoggingContext) : IO[K8SException, Scale]

    def updateScale[O <: ObjectResource](client: KubernetesClient, namespace: String, objName: String, scale: Scale)(implicit rd: ResourceDefinition[O], sc: Scale.SubresourceSpec[O], lc: LoggingContext): IO[K8SException, Scale]

    def patch[P <: Patch, O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, patchData: P)
                                              (implicit patchfmt: Writes[P], fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext = RequestLoggingContext()): IO[K8SException, O]

    def getPodLogSource(client: KubernetesClient, namespace: String, name: String, queryParams: Pod.LogQueryParams)(implicit lc: LoggingContext): IO[K8SException, Source[ByteString, _]]

    def exec(client: KubernetesClient,
             namespace: String,
             podName: String,
             command: Seq[String],
             containerName: Option[String] = None,
             stdin: Option[ZStream[Any, Nothing, String]] = None,
             stdout: Option[String => Task[Unit]] = None,
             stderr: Option[String => Task[Unit]] = None,
             tty: Boolean = false,
             maybeClose: Option[Promise[Unit]] = None)(implicit lc: LoggingContext): IO[K8SException, Unit]

    def getServerAPIVersions(client: KubernetesClient)(implicit lc: LoggingContext): IO[K8SException, List[String]]

    def resourceFromFile[A <: ObjectResource](fileName: String)(implicit fmt: Format[A]): Task[A]

    def close(client: KubernetesClient): IO[K8SException, Unit]
}