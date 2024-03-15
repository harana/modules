package com.harana.modules.projects

import zio.Task
import zio.macros.accessible

@accessible
trait Projects {
    def setup(namespace: String): Task[Unit]

    def startMonitoring(namespace: String): Task[Unit]

    def stopMonitoring(namespace: String): Task[Unit]
}