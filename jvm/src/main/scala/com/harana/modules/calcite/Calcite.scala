package com.harana.modules.calcite

import zio.Task
import zio.macros.accessible

@accessible
trait Calcite {

  def rewrite(userId: String, query: String): Task[String]

}