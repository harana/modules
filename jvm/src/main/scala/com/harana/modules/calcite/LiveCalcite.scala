package com.harana.modules.calcite

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.sdk.shared.models.common.User.UserId
import org.apache.calcite.config.Lex
import org.apache.calcite.sql.SqlKind._
import org.apache.calcite.sql.dialect.CalciteSqlDialect
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.sql._
import zio.{Task, ZLayer, ZIO}

import scala.jdk.CollectionConverters._

object LiveCalcite {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveCalcite(config, logger, micrometer)
  }
}

case class LiveCalcite(config: Config, logger: Logger, micrometer: Micrometer) extends Calcite {

  def rewrite(userId: UserId, query: String): Task[String] = {
    ZIO.attempt(parse(userId, query))
  }

  private val CONFIG = SqlParser.configBuilder.setLex(Lex.MYSQL).build


  private def parse(userId: String, sql: String): String = {
    val sqlParser = SqlParser.create(sql, CONFIG)
    val sqlNode = sqlParser.parseStmt

    sqlNode.getKind match {
      case INSERT =>
        val sqlInsert = sqlNode.asInstanceOf[SqlInsert]
        val source = sqlInsert.getSource.asInstanceOf[SqlSelect]
        parseSource(source, userId)

      case SELECT =>
        parseSource(sqlNode.asInstanceOf[SqlSelect], userId)

      case ORDER_BY =>
        println("Order by not currently supported")

      case _ =>
        throw new IllegalArgumentException("It must be an insert SQL, sql:" + sql)
    }

    sqlNode.toSqlString(CalciteSqlDialect.DEFAULT).getSql
  }


  private def parseSource(sqlSelect: SqlSelect, userId: String): Unit = {
    parseSelectList(sqlSelect.getSelectList, userId)
    parseFrom(sqlSelect.getFrom, userId) match {
      case Some(newIdentifier) => sqlSelect.setFrom(newIdentifier)
      case None =>
    }
  }


  private def parseSelectList(sqlNodeList: SqlNodeList, userId: String): Unit =
    sqlNodeList.asScala.foreach(parseSelect(_, userId))


  private def parseFrom(from: SqlNode, userId: String): Option[SqlIdentifier] =
    from.getKind match {
      case IDENTIFIER =>
        val identifier = from.asInstanceOf[SqlIdentifier]
        Some(identifier.setName(0, s"$userId.$identifier"))

      case AS =>
        val sqlBasicCall = from.asInstanceOf[SqlBasicCall]

        parseFrom(sqlBasicCall.getOperandList.asScala.head, userId) match {
          case Some(newIdentifier) => sqlBasicCall.setOperand(0, newIdentifier)
          case None =>
        }

        None

      case SELECT =>
        parseSource(from.asInstanceOf[SqlSelect], userId)
        None

      case JOIN =>
        val sqlJoin = from.asInstanceOf[SqlJoin]

        parseFrom(sqlJoin.getLeft, userId) match {
          case Some(newIdentifier) => sqlJoin.setLeft(newIdentifier)
          case None =>
        }

        parseFrom(sqlJoin.getRight, userId) match {
          case Some(newIdentifier) => sqlJoin.setRight(newIdentifier)
          case None =>
        }

        None

      case _ => None
    }


  private def parseSelect(sqlNode: SqlNode, userId: String): Unit =
    sqlNode.getKind match {
      case IDENTIFIER =>

      case AS =>
        val firstNode = sqlNode.asInstanceOf[SqlBasicCall].getOperandList.asScala.head
        parseSelect(firstNode, userId)

      case SELECT =>
        parseSource(sqlNode.asInstanceOf[SqlSelect], userId)

      case _ =>

    }
}