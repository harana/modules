package com.harana.modules.airtable

import dev.fuxing.airtable.AirtableRecord
import dev.fuxing.airtable.AirtableTable.{PaginationList, QuerySpec}
import zio.Task
import zio.macros.accessible

import java.util

@accessible
trait Airtable {

    def tableIterator(base: String, table: String, query: QuerySpec): Task[util.Iterator[AirtableRecord]]

    def listTable(base: String, table: String, query: QuerySpec): Task[PaginationList]

    def getRecord(base: String, table: String, id: String): Task[AirtableRecord]

    def createRecords(base: String, table: String, records: List[AirtableRecord]): Task[Unit]

    def patchRecords(base: String, table: String, records: List[AirtableRecord]): Task[Unit]

    def replaceRecords(base: String, table: String, records: List[AirtableRecord]): Task[Unit]

    def deleteRecords(base: String, table: String, recordIds: List[String]): Task[Unit]

}