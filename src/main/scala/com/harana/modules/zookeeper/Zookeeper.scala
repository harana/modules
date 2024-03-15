package com.harana.modules.zookeeper

import zio.Task
import zio.macros.accessible

@accessible
trait Zookeeper {

    def localStart: Task[Unit]

    def localStop: Task[Unit]

}