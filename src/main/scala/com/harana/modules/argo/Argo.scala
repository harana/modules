package com.harana.modules.argo

import com.harana.modules.argo.events.EventSource.EventSource
import com.harana.modules.argo.events.Rollout.Rollout
import com.harana.modules.argo.events.Sensor.Sensor
import com.harana.modules.argo.workflows.Workflow.Workflow
import com.harana.modules.argo.workflows.WorkflowTemplate.WorkflowTemplate
import skuber.api.client.KubernetesClient
import zio.Task
import zio.macros.accessible

@accessible
trait Argo {

    def createOrUpdateEventSource(namespace: String, eventSource: EventSource, client: Option[KubernetesClient] = None): Task[EventSource]
    def deleteEventSource(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Unit]
    def existsEventSource(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Boolean]

    def createOrUpdateRollout(namespace: String, rollout: Rollout, client: Option[KubernetesClient] = None): Task[Rollout]
    def deleteRollout(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Unit]
    def existsRollout(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Boolean]

    def createOrUpdateSensor(namespace: String, sensor: Sensor, client: Option[KubernetesClient] = None): Task[Sensor]
    def deleteSensor(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Unit]
    def existsSensor(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Boolean]

    def createOrUpdateWorkflow(namespace: String, workflow: Workflow, client: Option[KubernetesClient] = None): Task[Workflow]
    def deleteWorkflow(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Unit]
    def existsWorkflow(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Boolean]

    def createOrUpdateWorkflowTemplate(namespace: String, template: WorkflowTemplate, client: Option[KubernetesClient] = None): Task[WorkflowTemplate]
    def deleteWorkflowTemplate(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Unit]
    def existsWorkflowTemplate(namespace: String, name: String, client: Option[KubernetesClient] = None): Task[Boolean]

}