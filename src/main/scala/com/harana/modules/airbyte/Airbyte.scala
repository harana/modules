package com.harana.modules.airbyte

import io.airbyte.protocol.models.{AirbyteCatalog, AirbyteConnectionStatus, ConfiguredAirbyteCatalog}
import zio.macros.accessible
import zio.{Task, UIO}

@accessible
trait Airbyte {

    def integrations: Task[List[AirbyteIntegration]]

    def discover(integrationName: String, connectionValues: Map[String, Object]): Task[AirbyteCatalog]

    def check(integrationName: String, connectionValues: Map[String, Object]): Task[AirbyteConnectionStatus]

    def read(integrationName: String, catalog: ConfiguredAirbyteCatalog): Task[Unit]

}