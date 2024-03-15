package com.harana.modules.jsoup.models

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

trait Target[T] {
  val target: T
}

trait ElementTarget extends Target[Element]
trait ElementsTarget extends Target[Elements]