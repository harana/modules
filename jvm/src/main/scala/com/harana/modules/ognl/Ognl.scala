package com.harana.modules.ognl

import zio.Task
import zio.macros.accessible

@accessible
trait Ognl {
    def render(expression: String, context: Map[String, Any]): Task[Any]
}