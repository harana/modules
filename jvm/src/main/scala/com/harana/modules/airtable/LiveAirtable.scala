package com.harana.modules.airtable

import com.harana.modules.airtable.LiveAirtable.clientRef
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import dev.fuxing.airtable.AirtableTable.{PaginationList, QuerySpec}
import dev.fuxing.airtable.{AirtableApi, AirtableRecord}
import zio.{Task, ZIO, ZLayer}

import java.util
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._


object LiveAirtable {

  private val clientRef = new AtomicReference[Option[AirtableApi]](None)

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAirtable(config, logger, micrometer)
  }
}

case class LiveAirtable(config: Config, logger: Logger, micrometer: Micrometer) extends Airtable {

    private def client =
      for {
        client            <- if (clientRef.get.nonEmpty) ZIO.attempt(clientRef.get.get) else
                            for {
                              key             <- config.secret("airtable-key")
                              api             <- ZIO.attempt(new AirtableApi(key))
                            } yield api
        _                 =  clientRef.set(Some(client))
      } yield client


    def tableIterator(base: String, table: String, query: QuerySpec): Task[util.Iterator[AirtableRecord]] =
      for {
        api       <- client
        iterator  =  api.base(base).table(table).iterator(query)
      } yield iterator


    def listTable(base: String, table: String, query: QuerySpec): Task[PaginationList] =
      for {
        api       <- client
        list      <- ZIO.attempt(api.base(base).table(table).list(query))
      } yield list


    def getRecord(base: String, table: String, id: String): Task[AirtableRecord] =
      for {
        api       <- client
        record    =  api.base(base).table(table).get(id)
      } yield record


    def createRecords(base: String, table: String, records: List[AirtableRecord]): Task[Unit] =
      for {
        api       <- client
        _         <- ZIO.attempt(api.base(base).table(table).post(records.asJava))
      } yield ()


    def patchRecords(base: String, table: String, records: List[AirtableRecord]): Task[Unit] =
      for {
        api       <- client
        _  <- ZIO.attempt(api.base(base).table(table).patch(records.asJava))
      } yield ()


    def replaceRecords(base: String, table: String, records: List[AirtableRecord]): Task[Unit] =
      for {
        api       <- client
        _         <- ZIO.attempt(api.base(base).table(table).put(records.asJava))
      } yield ()


    def deleteRecords(base: String, table: String, recordIds: List[String]): Task[Unit] =
      for {
        api       <- client
        _         <- ZIO.attempt(api.base(base).table(table).delete(recordIds.asJava))
      } yield ()
}