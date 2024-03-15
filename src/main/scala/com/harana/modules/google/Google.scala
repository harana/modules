package com.harana.modules.google

import zio.Task
import zio.macros.accessible

@accessible
trait Google {

    def pageView(clientId: String, page: String, title: String): Task[String]

    def event(clientId: String, category: String, action: String, label: String, value: String): Task[Event]

    def exception(clientId: String, description: String, fatal: Boolean): Task[String]

    def time(clientId: String, category: String, variable: String, time: Long, label: String): Task[Event]

    def send(event: Event): Task[Unit]

    def batch(events: List[Event]): Task[Unit]

}