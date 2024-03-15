package com.harana.modules.meilisearch

import com.meilisearch.sdk.{Client, SearchRequest}
import io.circe.Encoder
import zio.Task
import zio.macros.accessible

@accessible
trait Meilisearch {

    def newClient(host: String, port: Option[Long] = None, apiKey: Option[String] = None): Task[Client]

    def createIndex(client: Client, index: String, primaryKey: Option[String] = None): Task[Unit]

    def deleteIndex(client: Client, index: String): Task[Unit]

    def addObjects[T](client: Client, index: String, objects: List[T], inBatches: Boolean = false)(implicit encoder: Encoder[T]): Task[Unit]

    def updateObjects[T](client: Client, index: String, objects: List[T], inBatches: Boolean = false)(implicit encoder: Encoder[T]): Task[Unit]

    def deleteObjects(client: Client, index: String, ids: List[String]): Task[Unit]

    def deleteAllObjects(client: Client, index: String): Task[Unit]

    def search(client: Client, index: String, query: String): Task[List[Map[String, AnyRef]]]

    def search(client: Client, index: String, request: SearchRequest): Task[Unit]

    def stopWords(client: Client, index: String): Task[List[String]]

    def updateStopWords(client: Client, index: String, stopWords: List[String]): Task[Unit]

}