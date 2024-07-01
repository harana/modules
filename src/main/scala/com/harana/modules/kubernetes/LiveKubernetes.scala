package com.harana.modules.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.harana.modules.core.app.App.runEffect
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.kubernetes.LiveKubernetes._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.util.ByteString
import play.api.libs.json.{Format, Json, Writes}
import skuber.api.Configuration
import skuber.api.client.{KubernetesClient, LoggingContext, RequestLoggingContext, WatchEvent}
import skuber.api.patch.Patch
import skuber.apiextensions.v1.CustomResourceDefinition
import skuber.json.format.namespaceFormat
import skuber.{K8SException, k8sInit, _}
import zio.interop.reactivestreams.streamToPublisher
import zio.{Clock, _}
import zio.stream.ZStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

object LiveKubernetes {
  val yamlReader = new ObjectMapper(new YAMLFactory)
  val jsonWriter = new ObjectMapper()

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveKubernetes(config, logger, micrometer)
  }
}

case class LiveKubernetes(config: Config, logger: Logger, micrometer: Micrometer) extends Kubernetes {

  def newClient: IO[K8SException, KubernetesClient] = {
    val cld = classOf[ActorSystem].getClassLoader
    implicit val system = ActorSystem("Kubernetes", classLoader = Some(cld))
    ZIO.attempt(k8sInit(Configuration.parseKubeconfigFile().get)).refineToOrDie[K8SException]
  }


  def get[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Option[O]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).getOption[O](name)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def exists[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Boolean] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).getOption(name)(fmt, rd, lc).map(_.nonEmpty) }.refineToOrDie[K8SException]


  def save(client: KubernetesClient, namespace: String, crd: CustomResourceDefinition): IO[K8SException, CustomResourceDefinition] =
    ZIO.fromFuture { _ =>
      client.usingNamespace(namespace).create(crd).recoverWith {
        case alreadyExists: K8SException if alreadyExists.status.code.contains(409) =>
          client.get[CustomResourceDefinition](crd.name).flatMap { existing =>
            val currentVersion = existing.metadata.resourceVersion
            val newMeta = crd.metadata.copy(resourceVersion = currentVersion)
            val updatedObj = crd.copy(metadata = newMeta)
            client.update(updatedObj)
        }
      }

    }.refineToOrDie[K8SException]


  def createNamespace(client: KubernetesClient, namespace: String)(implicit lc: LoggingContext): IO[K8SException, Namespace] =
    ZIO.fromFuture { _ =>  client.create[Namespace](Namespace(namespace)) }.refineToOrDie[K8SException]


  def create[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, O] =
    ZIO.fromFuture { _ => client.usingNamespace(namespace).create[O](obj)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def createPodAndWait(client: KubernetesClient, namespace: String, obj: Pod, startupTime: Long)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Pod] =
    for {
      pod                   <-  create[Pod](client, namespace, obj)
      schedule              =   Schedule.fixed(500.milliseconds) && Schedule.recurUntil[Boolean](running => running == true)
      _                     <-  podInState(client, namespace, obj.name, "running").repeat(schedule)
    } yield pod


  def podTerminating(client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Boolean] =
    get[Pod](client, namespace, name).map(pod => pod.flatMap(p => p.metadata.deletionTimestamp).nonEmpty)


  def waitForPodToTerminate(client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Unit] =
    for {
      schedule              <-  ZIO.succeed(Schedule.fixed(500.milliseconds) && Schedule.recurWhile[Boolean](terminating => terminating == true))
      _                     <-  (for {
                                  terminating   <- podTerminating(client, namespace, name)
                                  exists        <- exists[Pod](client, namespace, name)
                                } yield terminating && exists).repeat(schedule)
    } yield ()


  def podInState(client: KubernetesClient, namespace: String, name: String, desiredState: String)(implicit fmt: Format[Pod], rd: ResourceDefinition[Pod], lc: LoggingContext): IO[K8SException, Boolean] =
    get[Pod](client, namespace, name).map(maybePod =>
      for {
        pod                 <- maybePod
        status              <- pod.status
        containerStatus     <- status.containerStatuses.headOption
        containerState      <- containerStatus.state
        ready               =  containerState.id == desiredState
      } yield ready
    ).map(_.getOrElse(false))


  def update[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, O] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).update[O](obj)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def delete[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, gracePeriodSeconds: Int = -1)(implicit rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Unit] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).delete[O](name, gracePeriodSeconds)(rd, lc) }.refineToOrDie[K8SException]


  def deleteWithOptions[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, options: DeleteOptions)(implicit rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Unit] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).deleteWithOptions[O](name, options)(rd, lc) }.refineToOrDie[K8SException]


  def deleteAll[O <: ObjectResource](client: KubernetesClient, namespace: String)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).deleteAll[ListResource[O]]()(fmt, rd, lc) }.refineToOrDie[K8SException]


  def deleteAllSelected[O <: ObjectResource](client: KubernetesClient, namespace: String, labelSelector: LabelSelector)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).deleteAllSelected[ListResource[O]](labelSelector)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def getNamespaceNames(client: KubernetesClient)(implicit lc: LoggingContext): IO[K8SException, List[String]] =
    ZIO.fromFuture { _ =>  client.getNamespaceNames(lc) }.refineToOrDie[K8SException]


  def listByNamespace[O <: ObjectResource](client: KubernetesClient)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, Map[String, ListResource[O]]] =
    ZIO.fromFuture { _ =>  client.listByNamespace[ListResource[O]]()(fmt, rd, lc) }.refineToOrDie[K8SException]


  def list[O <: ObjectResource](client: KubernetesClient, namespace: String)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).list[ListResource[O]]()(fmt, rd, lc) }.refineToOrDie[K8SException]


  def listSelected[O <: ObjectResource](client: KubernetesClient, namespace: String, labelSelector: LabelSelector)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).listSelected[ListResource[O]](labelSelector)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def listWithOptions[O <: ObjectResource](client: KubernetesClient, namespace: String, options: ListOptions)(implicit fmt: Format[ListResource[O]], rd: ResourceDefinition[ListResource[O]], lc: LoggingContext): IO[K8SException, ListResource[O]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).listWithOptions[ListResource[O]](options)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def updateStatus[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], statusEv: HasStatusSubresource[O], lc: LoggingContext): IO[K8SException, O] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).updateStatus[O](obj)(fmt, rd, statusEv, lc) }.refineToOrDie[K8SException]


  def getStatus[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String)(implicit fmt: Format[O], rd: ResourceDefinition[O], statusEv: HasStatusSubresource[O], lc: LoggingContext): IO[K8SException, O] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).getStatus[O](name)(fmt, rd, statusEv, lc) }.refineToOrDie[K8SException]


  def watch[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).watch[O](obj)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def watch[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).watch[O](name, sinceResourceVersion, bufSize)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def watchAll[O <: ObjectResource](client: KubernetesClient, namespace: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).watchAll[O](sinceResourceVersion, bufSize)(fmt, rd, lc) }.refineToOrDie[K8SException]


  def watchContinuously[O <: ObjectResource](client: KubernetesClient, namespace: String, obj: O)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.from(client.usingNamespace(namespace).watchContinuously[O](obj)(fmt, rd, lc)).refineToOrDie[K8SException]


  def watchContinuously[O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.from(client.usingNamespace(namespace).watchContinuously[O](name, sinceResourceVersion, bufSize)(fmt, rd, lc)).refineToOrDie[K8SException]


  def watchAllContinuously[O <: ObjectResource](client: KubernetesClient, namespace: String, sinceResourceVersion: Option[String] = None, bufSize: Int = 10000)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.from(client.usingNamespace(namespace).watchAllContinuously[O](sinceResourceVersion, bufSize)(fmt, rd, lc)).refineToOrDie[K8SException]


  def watchWithOptions[O <: ObjectResource](client: KubernetesClient, namespace: String, options: ListOptions, bufSize: Int = 10000)(implicit fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext): IO[K8SException, Source[WatchEvent[O], _]] =
    ZIO.from(client.usingNamespace(namespace).watchWithOptions[O](options, bufSize)(fmt, rd, lc)).refineToOrDie[K8SException]


  def getScale[O <: ObjectResource](client: KubernetesClient, namespace: String, objName: String)(implicit rd: ResourceDefinition[O], sc: Scale.SubresourceSpec[O], lc: LoggingContext) : IO[K8SException, Scale] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).getScale[O](objName)(rd, sc, lc) }.refineToOrDie[K8SException]


  def updateScale[O <: ObjectResource](client: KubernetesClient, namespace: String, objName: String, scale: Scale)(implicit rd: ResourceDefinition[O], sc: Scale.SubresourceSpec[O], lc: LoggingContext): IO[K8SException, Scale] =
    ZIO.fromFuture { _ =>  client.usingNamespace(namespace).updateScale[O](objName, scale)(rd, sc, lc) }.refineToOrDie[K8SException]


  def patch[P <: Patch, O <: ObjectResource](client: KubernetesClient, namespace: String, name: String, patchData: P)(implicit patchfmt: Writes[P], fmt: Format[O], rd: ResourceDefinition[O], lc: LoggingContext = RequestLoggingContext()): IO[K8SException, O] =
    ZIO.fromFuture { _ =>  client.patch[P, O](name, patchData, Some(namespace))(patchfmt, fmt, rd, lc) }.refineToOrDie[K8SException]


  def getPodLogSource(client: KubernetesClient, namespace: String, name: String, queryParams: Pod.LogQueryParams)(implicit lc: LoggingContext): IO[K8SException, Source[ByteString, _]] =
    ZIO.fromFuture { _ =>  client.getPodLogSource(name, queryParams, Some(namespace))(lc) }.refineToOrDie[K8SException]


  def exec(client: KubernetesClient,
           namespace: String,
           podName: String,
           command: Seq[String],
           containerName: Option[String] = None,
           stdin: Option[ZStream[Any, Nothing, String]] = None,
           stdout: Option[String => Task[Unit]] = None,
           stderr: Option[String => Task[Unit]] = None,
           tty: Boolean = false,
           close: Option[Promise[Unit]] = None)(implicit lc: LoggingContext): IO[K8SException, Unit] =
    for {
      source    <- if (stdin.nonEmpty)
                    for {
                      publisher <- stdin.get.toPublisher
                      source    =  Some(Source.fromPublisher(publisher))
                    } yield source
                   else ZIO.none

      sinkOut   =  if (stdout.nonEmpty) Some(Sink.foreach[String](s => runEffect(stdout.get(s)))) else None
      sinkErr   =  if (stderr.nonEmpty) Some(Sink.foreach[String](s => runEffect(stderr.get(s)))) else None

      _         <- ZIO.fromFuture { _ =>
                     client.usingNamespace(namespace).exec(podName, command, containerName, source, sinkOut, sinkErr, tty, close)(lc)
                   }.refineToOrDie[K8SException]
    } yield ()


  def getServerAPIVersions(client: KubernetesClient)(implicit lc: LoggingContext): IO[K8SException, List[String]] =
    ZIO.fromFuture { _ =>  client.getServerAPIVersions(lc) }.refineToOrDie[K8SException]


  def resourceFromFile[A <: ObjectResource](fileName: String)(implicit fmt: Format[A]): Task[A] =
    for {
      yaml        <- ZIO.attempt(scala.io.Source.fromResource(fileName).mkString)
      obj         <- ZIO.attempt(yamlReader.readValue(yaml, classOf[Object]))
                    .onError(ex => logger.info(s"Failed to parse YAML for: $fileName with message: ${ex.prettyPrint}"))
      json        <- ZIO.attempt(jsonWriter.writeValueAsString(obj))
      resource    <- ZIO.attempt(Json.parse(json).as[A])
                    .onError(ex => logger.info(s"Failed to convert YAML to object for: $fileName with message: ${ex.prettyPrint}"))
    } yield resource


  def close(client: KubernetesClient): IO[K8SException, Unit] =
    ZIO.from(client.close).refineToOrDie[K8SException]
}