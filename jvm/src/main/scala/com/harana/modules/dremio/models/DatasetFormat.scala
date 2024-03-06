package com.harana.modules.dremio.models

import io.circe._
import org.latestbit.circe.adt.codec._

// FIXME
//sealed trait DatasetFormat
//object DatasetFormat {
//
//  implicit val encoder : Encoder[DatasetFormat] = JsonTaggedAdtCodec.createEncoder[DatasetFormat]("type")
//  implicit val decoder : Decoder[DatasetFormat] = JsonTaggedAdtCodec.createDecoder[DatasetFormat]("type")
//
//  case class Excel(sheetName: String,
//                   extractHeader: Boolean,
//                   hasMergedCells: Boolean) extends DatasetFormat
//
//  case class JSON() extends DatasetFormat
//
//  case class Parquet() extends DatasetFormat
//
//  case class Text(fieldDelimiter: String,
//                  lineDelimiter: String,
//                  quote: String,
//                  comment: String,
//                  escape: String,
//                  skipFirstLine: Boolean,
//                  extractHeader: Boolean,
//                  trimHeader: Boolean,
//                  autoGenerateColumnNames: Boolean) extends DatasetFormat
//
//  case class XLS(sheetName: String,
//                 extractHeader: Boolean,
//                 hasMergedCells: Boolean) extends DatasetFormat
//}
