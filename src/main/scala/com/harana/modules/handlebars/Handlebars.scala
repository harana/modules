package com.harana.modules.handlebars

import zio.Task
import zio.macros.accessible

@accessible
trait Handlebars {

  def renderPath(path: String, props: Map[String, Object]): Task[String]

  def renderString(name: String, props: Map[String, Object]): Task[String]

}