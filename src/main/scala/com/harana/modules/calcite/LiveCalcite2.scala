package com.harana.modules.calcite

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.sdk.shared.models.common.User.UserId
import org.apache.calcite.sql.SqlKind._
import org.apache.calcite.sql.dialect.CalciteSqlDialect
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.sql._
import zio.{Task, ZLayer, ZIO}

import scala.collection.mutable.ListBuffer

object LiveCalcite2 {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveCalcite2(config, logger, micrometer)
  }
}

case class LiveCalcite2(config: Config, logger: Logger, micrometer: Micrometer) extends Calcite {

  def rewrite(userId: UserId, query: String): Task[String] =
    for {
      node        <- ZIO.attempt(SqlParser.create(query).parseQuery())
      sqlNode     <- ZIO.attempt(tableNames(userId, node))
    } yield sqlNode.toSqlString(CalciteSqlDialect.DEFAULT).getSql

  private def tableNames(userId: UserId, sqlNode: SqlNode): SqlNode = {
    val node = if (sqlNode.getKind.equals(ORDER_BY)) sqlNode.asInstanceOf[SqlOrderBy].query else sqlNode
    processFrom(userId, node)
  }

  private def processFrom(userId: UserId, node: SqlNode): SqlNode = {
    val childNode = node.asInstanceOf[SqlSelect].getFrom
    if (childNode == null) return node

    childNode.getKind match {
      case IDENTIFIER =>
        println(childNode.toSqlString(CalciteSqlDialect.DEFAULT))
        val identifier = childNode.asInstanceOf[SqlIdentifier]
        val newIdentifier = identifier.setName(0, s"$userId.${identifier.names.get(0)}")
        node.asInstanceOf[SqlSelect].setFrom(newIdentifier)
        node
      case AS =>
        println(childNode.toSqlString(CalciteSqlDialect.DEFAULT))
        val call = childNode.asInstanceOf[SqlBasicCall]
        if (call.operand(0).isInstanceOf[SqlIdentifier]) {
          val newIdentifier = setName(userId, call.operand(0).asInstanceOf[SqlIdentifier])
          call.setOperand(0, newIdentifier)
          node.asInstanceOf[SqlSelect].setFrom(call)
        }
        node

      case JOIN =>
        println(childNode.toSqlString(CalciteSqlDialect.DEFAULT))
        val fromNode = childNode.asInstanceOf[SqlJoin]

        if (fromNode.getLeft.getKind.equals(AS)) {
          val newLeftIdentifier = setName(userId, leftNode(fromNode))
          val newRightIdentifier = setName(userId, rightNode(fromNode))
          fromNode.getLeft.asInstanceOf[SqlBasicCall].setOperand(0, newLeftIdentifier)
          fromNode.getRight.asInstanceOf[SqlBasicCall].setOperand(0, newRightIdentifier)
          node
        }
        else {
          val tables = ListBuffer[String]()

          fromNode.getLeft.getKind match {
            case IDENTIFIER =>
              if (fromNode.getRight.getKind.equals(IDENTIFIER)) {
                val newLeftIdentifier = setName(userId, fromNode.getLeft.asInstanceOf[SqlIdentifier])
                val newRightIdentifier = setName(userId, fromNode.getRight.asInstanceOf[SqlIdentifier])
                fromNode.setLeft(newLeftIdentifier)
                fromNode.setRight(newRightIdentifier)
                node
              } else {
                println(fromNode.getLeft.toString)
                node
              }

            case JOIN =>
              var leftJoin = fromNode.getLeft.asInstanceOf[SqlJoin]

              while (!leftJoin.getLeft.getKind.equals(AS) && leftJoin.getLeft.isInstanceOf[SqlJoin]) {
                //                  tables += rightNode(leftJoin)
                leftJoin = leftJoin.getLeft.asInstanceOf[SqlJoin]
              }

              if (leftJoin.getLeft.isInstanceOf[SqlBasicCall])
                println(s"Left C = ${leftNode(leftJoin)}")

              if (leftJoin.getRight.isInstanceOf[SqlBasicCall])
                println(s"Right C = ${rightNode(leftJoin)}")

              node
            //tables.toList ++ List(left(leftJoin), right(leftJoin))
          }
        }
    }
  }

  private def setName(userId: UserId, identifier: SqlIdentifier) =
    identifier.setName(0, s"$userId.${identifier.names.get(0)}")

  private def leftNode(node: SqlJoin) =
    node.getLeft.asInstanceOf[SqlBasicCall].operand(0).asInstanceOf[SqlIdentifier]

  private def rightNode(node: SqlJoin) =
    node.getRight.asInstanceOf[SqlBasicCall].operand(0).asInstanceOf[SqlIdentifier]
}