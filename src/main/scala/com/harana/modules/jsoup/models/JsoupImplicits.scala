package com.harana.modules.jsoup.models

import org.jsoup.nodes._
import org.jsoup.select.Elements

import scala.jdk.CollectionConverters._

trait JsoupImplicits {
  implicit def enrichElements(xs: Elements): RichElements = new RichElements(xs)
  implicit def enrichElement(el: Element): RichElement = new RichElement(el)
  implicit def enrichNodeList[N <: Node](l: java.util.List[N]): RichNodeList[N] = new RichNodeList(l)
}

object JsoupImplicits extends JsoupImplicits

class RichElements(val target: Elements)
  extends Iterable[Element]
    with ClosestElements
    with ElementsAttributeOption {

  def iterator: Iterator[Element] = {
    target.asScala.iterator
  }
}

class RichElement(val target: Element)
  extends ClosestElement
    with DocumentPositioning
    with AttributeOption {
}

class RichNodeList[N <: Node](val target: java.util.List[N]) extends Iterable[Node] {
  def iterator: Iterator[Node] = {
    target.asScala.iterator
  }
}