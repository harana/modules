package com.harana.modules.ognl

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.ognl.models.{OgnlMemberAccess, OgnlObjectPropertyAccessor}
import ognl.{DefaultClassResolver, OgnlRuntime, Ognl => jOgnl}
import zio.{Task, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object LiveOgnl {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveOgnl(config, logger, micrometer)
  }
}

case class LiveOgnl(config: Config, logger: Logger, micrometer: Micrometer) extends Ognl {

  OgnlRuntime.setPropertyAccessor(classOf[Object], new OgnlObjectPropertyAccessor())

    def render(expression: String, context: Map[String, Any]): Task[Any] = {
      val ognlContext = jOgnl.createDefaultContext(context.asJava, new OgnlMemberAccess, new DefaultClassResolver, null)
      val ognlExpression = jOgnl.parseExpression(expression)
      ZIO.succeed(jOgnl.getValue(ognlExpression, ognlContext, context.asJava))
    }

}