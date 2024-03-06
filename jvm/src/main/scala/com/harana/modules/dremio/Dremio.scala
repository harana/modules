package com.harana.modules.dremio

import com.harana.modules.dremio.models._
import io.circe.Decoder
import zio.Task
import zio.macros.accessible

@accessible
trait Dremio {

    def jobStatus(id: JobId): Task[JobStatus]
    def jobResults(id: JobId, offset: Option[Int], limit: Option[Int]): Task[JobResults]
    def cancelJob(id: JobId): Task[Unit]

    def getCatalog: Task[List[EntitySummary]]
    def getCatalogEntity[E <: Entity](idOrPath: Either[EntityId, String])(implicit d: Decoder[E], m: Manifest[E]): Task[E]
    def getCatalogEntityTags(id: EntityId): Task[List[String]]
    def getCatalogEntityWiki(id: EntityId): Task[String]

    def updateCatalogEntity[E <: Entity](id: EntityId, entity: E): Task[Unit]
    def updateCatalogEntityTags(id: EntityId, tags: List[String]): Task[Unit]
    def updateCatalogEntityWiki(id: EntityId, text: String): Task[Unit]
    def deleteCatalogEntity(id: EntityId): Task[Unit]

    def refreshCatalogEntity(id: EntityId): Task[Unit]

    def sql(sql: String): Task[JobId]

}