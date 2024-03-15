package com.harana.modules.handlebars

import com.github.jknack.handlebars.{Context, Handlebars => CoreHandlebars}
import com.github.jknack.handlebars.context.{JavaBeanValueResolver, MapValueResolver, MethodValueResolver}
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.{Task, ZIO, ZLayer}

object LiveHandlebars {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveHandlebars(config, logger, micrometer)
  }
}

case class LiveHandlebars(config: Config, logger: Logger, micrometer: Micrometer) extends Handlebars {

    private val handlebars = {
      val l = new ClassPathTemplateLoader
      l.setPrefix("/templates/")
      l.setSuffix(".hbs")
      val hb = new CoreHandlebars(l)
      hb.registerHelper("each", ScalaEachHelper)
      hb.infiniteLoops(true)
    }

    def renderPath(name: String, props: Map[String, Object]): Task[String] =
      ZIO.attempt(handlebars.compile(name)(context(props)))


    def renderString(content: String, props: Map[String, Object]): Task[String] =
      ZIO.attempt(handlebars.compileInline(content)(context(props)))


    private def context(props: Map[String, Object]) =
      Context
      .newBuilder(props)
      .resolver(ScalaResolver, MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE
      ).build()

}