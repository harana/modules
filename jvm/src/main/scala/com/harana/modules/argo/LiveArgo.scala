package com.harana.modules.argo

import com.harana.modules.argo.events.EventSource._
import com.harana.modules.argo.events.Rollout._
import com.harana.modules.argo.events.Sensor._
import com.harana.modules.argo.workflows.Workflow._
import com.harana.modules.argo.workflows.WorkflowTemplate._
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.kubernetes.Kubernetes
import play.api.libs.json.Format
import skuber.ResourceDefinition
import skuber.api.client.{KubernetesClient, LoggingContext}
import zio.{Task, ZIO, ZLayer}

import scala.reflect.ClassTag

object LiveArgo {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      kubernetes    <- ZIO.service[Kubernetes]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveArgo(config, kubernetes, logger, micrometer)
  }
}

case class LiveArgo(config: Config, kubernetes: Kubernetes, logger: Logger, micrometer: Micrometer) extends Argo {

  def createOrUpdateEventSource(namespace: String, eventSource: EventSource, client: Option[KubernetesClient]): Task[EventSource] =
    upsert[EventSource](namespace, eventSource, client)


  def deleteEventSource(namespace: String, name: String, client: Option[KubernetesClient]): Task[Unit] =
    delete[EventSource](namespace, name, client)


  def existsEventSource(namespace: String, name: String, client: Option[KubernetesClient]): Task[Boolean] =
    exists[EventSource](namespace, name, client)


  def createOrUpdateRollout(namespace: String, rollout: Rollout, client: Option[KubernetesClient]): Task[Rollout] =
    upsert[Rollout](namespace, rollout, client)


  def deleteRollout(namespace: String, name: String, client: Option[KubernetesClient]): Task[Unit] =
    delete[Rollout](namespace, name, client)


  def existsRollout(namespace: String, name: String, client: Option[KubernetesClient]): Task[Boolean] =
    exists[Rollout](namespace, name, client)


  def createOrUpdateSensor(namespace: String, sensor: Sensor, client: Option[KubernetesClient]): Task[Sensor] =
    upsert[Sensor](namespace, sensor, client)


  def deleteSensor(namespace: String, name: String, client: Option[KubernetesClient]): Task[Unit] =
    delete[Sensor](namespace, name, client)


  def existsSensor(namespace: String, name: String, client: Option[KubernetesClient]): Task[Boolean] =
    exists[Sensor](namespace, name, client)


  def createOrUpdateWorkflow(namespace: String, workflow: Workflow, client: Option[KubernetesClient]): Task[Workflow] =
    upsert[Workflow](namespace, workflow, client)


  def deleteWorkflow(namespace: String, name: String, client: Option[KubernetesClient]): Task[Unit] =
    delete[Workflow](namespace, name, client)


  def existsWorkflow(namespace: String, name: String, client: Option[KubernetesClient]): Task[Boolean] =
    exists[Workflow](namespace, name, client)


  def createOrUpdateWorkflowTemplate(namespace: String, template: WorkflowTemplate, client: Option[KubernetesClient]): Task[WorkflowTemplate] =
    upsert[WorkflowTemplate](namespace, template, client)


  def deleteWorkflowTemplate(namespace: String, name: String, client: Option[KubernetesClient]): Task[Unit] =
    delete[WorkflowTemplate](namespace, name, client)


  def existsWorkflowTemplate(namespace: String, name: String, client: Option[KubernetesClient]): Task[Boolean] =
    exists[WorkflowTemplate](namespace, name, client)


  private def upsert[A <: skuber.ObjectResource](namespace: String, resource: A, client: Option[KubernetesClient])(implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext, ct: ClassTag[A]): Task[A] =
    for {
      client        <- ZIO.fromOption(client).orElse(kubernetes.newClient)
      exists        <- kubernetes.exists[A](client, namespace, resource.name)
      resource      <- if (exists) kubernetes.create[A](client, namespace, resource) else kubernetes.update[A](client, namespace, resource)
      _             <- kubernetes.close(client)
    } yield resource


  private def rename[A <: skuber.ObjectResource](namespace: String, oldName: String, newName: String, client: Option[KubernetesClient])(implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext, ct: ClassTag[A]): Task[Unit] =
    for {
      client        <- ZIO.fromOption(client).orElse(kubernetes.newClient)
      exists        <- kubernetes.exists[A](client, namespace, oldName)
      resource      <- kubernetes.get[A](client, namespace, oldName)
      _             <- ZIO.when(exists)(kubernetes.delete[A](client, namespace, oldName))
      _             <- ZIO.when(exists)(kubernetes.create[A](client, namespace, resource.get))
      _             <- kubernetes.close(client)
    } yield ()


  private def delete[A <: skuber.ObjectResource](namespace: String, name: String, client: Option[KubernetesClient])(implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext, ct: ClassTag[A]): Task[Unit] =
    for {
      client        <- ZIO.fromOption(client).orElse(kubernetes.newClient)
      _             <- kubernetes.delete[A](client, namespace, name)
      _             <- kubernetes.close(client)
    } yield ()


  private def exists[A <: skuber.ObjectResource](namespace: String, name: String, client: Option[KubernetesClient])(implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext, ct: ClassTag[A]): Task[Boolean] =
    for {
      client        <- ZIO.fromOption(client).orElse(kubernetes.newClient)
      exists        <- kubernetes.get[A](client, namespace, name).map(_.nonEmpty)
    } yield exists

}