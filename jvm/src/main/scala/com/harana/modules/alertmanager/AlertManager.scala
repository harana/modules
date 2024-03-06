package com.harana.modules.alertmanager

import zio.Task
import zio.macros.accessible

@accessible
trait AlertManager {

    def start(name: String,
              storageClassName: String,
              replicas: Int = 1): Task[Unit]

    def healthy: Task[Boolean]
    def ready: Task[Boolean]
    def reload: Task[Unit]

    def status: Task[AlertManagerStatus]

    def receivers: Task[List[ReceiverName]]

    def silences(filters: Set[String] = Set()): Task[List[Silence]]
    def silence(id: SilenceId): Task[Silence]
    def saveSilence(silence: PostableSilence): Task[SilenceId]
    def deleteSilence(id: SilenceId): Task[Unit]

    def alerts(active: Boolean = false,
               silenced: Boolean = false,
               inhibited: Boolean = false,
               unprocessed: Boolean = false,
               filters: List[String] = List(),
               receiver: Option[String] = None): Task[List[Alert]]

    def saveAlerts(alerts: List[PostableAlert]): Task[Unit]

    def alertGroups(active: Boolean = false,
                    silenced: Boolean = false,
                    inhibited: Boolean = false,
                    filters: List[String] = List(),
                    receiver: Option[String] = None): Task[List[AlertGroup]]

}