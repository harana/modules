package com.harana.modules.meilisearch

import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.meilisearch.sdk.model.TaskInfo
import com.meilisearch.sdk.{Client, Config, SearchRequest, TasksHandler}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import zio.{Clock, Schedule, ZIO, ZLayer, durationInt}
import zio.Duration._

import scala.jdk.CollectionConverters._

object LiveMeilisearch {
    val layer = ZLayer {
        for {
            config        <- ZIO.service[Config]
            logger        <- ZIO.service[Logger]
            micrometer    <- ZIO.service[Micrometer]
        } yield LiveMeilisearch(config, logger, micrometer)
    }
}

case class LiveMeilisearch(config: Config, logger: Logger, micrometer: Micrometer) extends Meilisearch {

    def newClient(host: String, port: Option[Long] = None, apiKey: Option[String] = None) =
        ZIO.attempt {
            val url = s"http://$host:${port.getOrElse(7000)}"
            new Client(if (apiKey.isEmpty) new Config(url) else new Config(url, apiKey.get))
        }


    def createIndex(client: Client, index: String, primaryKey: Option[String] = None) =
        executeTask(client, if (primaryKey.isEmpty) client.createIndex(index) else client.createIndex(index, primaryKey.get))


    def deleteIndex(client: Client, index: String) =
        executeTask(client, client.deleteIndex(index))


    def addObjects[T](client: Client, index: String, objects: List[T], inBatches: Boolean = false)(implicit encoder: Encoder[T]) =
        executeTask(client, client.index(index).addDocuments(objects.asJson.noSpaces))


    def updateObjects[T](client: Client, index: String, objects: List[T], inBatches: Boolean = false)(implicit encoder: Encoder[T]) =
        executeTask(client, client.index(index).updateDocuments(objects.asJson.noSpaces))


    def deleteObjects(client: Client, index: String, ids: List[String]) =
        executeTask(client, client.index(index).deleteDocuments(ids.asJava))


    def deleteAllObjects(client: Client, index: String) =
        executeTask(client, client.index(index).deleteAllDocuments())


    def search(client: Client, index: String, query: String) =
        ZIO.attempt(client.index(index).search(query).getHits.asScala.map(_.asScala.toMap).toList)


    def search(client: Client, index: String, request: SearchRequest) =
        ZIO.attempt(client.index(index).search(request).getHits.asScala.map(_.asScala.toMap).toList)


    def stopWords(client: Client, index: String) =
        ZIO.attempt(client.index(index).getStopWordsSettings.toList)


    def updateStopWords(client: Client, index: String, stopWords: List[String]) =
        ZIO.attempt(client.index(index).updateStopWordsSettings(stopWords.toArray))


    private def executeTask(client: Client, fn: => TaskInfo) =
        for {
            id          <- ZIO.succeed(fn.getTaskUid)
            schedule    =  Schedule.fixed(50 milliseconds) && Schedule.recurUntil[String](status =>
                             status != TasksHandler.SUCCEEDED && status != TasksHandler.FAILED
                           )
            _           <- ZIO.attempt(client.getTask(id).getStatus).repeat(schedule)
        } yield ()
}