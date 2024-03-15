package com.harana.modules.jsoup.models

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object ClosestFinder extends JsoupImplicits {

  def findClosestOption(selector: String, elem: Element): Option[Element] = {
    enrichElements(elem.select(selector)).headOption.orElse {
      elem.parents.headOption.flatMap { _ =>
        findClosestOption(selector, elem.parents)
      }
    }
  }

  def findClosestOption(selector: String, elems: Elements): Option[Element] = {
    elems.select(selector).headOption.orElse {
      elems.parents.headOption.flatMap { _ =>
        findClosestOption(selector, elems.parents)
      }
    }
  }

  /** Returns an Elements - i.e. it doesn't just grab the first */
  def findClosest(selector: String, elems: Elements): Elements = {
    elems.headOption.fold(elems) { _ =>
      val here = elems.select(selector)
      here.headOption.fold(findClosest(selector, elems.parents))(_ => here)
    }
  }

  def findClosestBeforeOption(selector: String, elem: Element): Option[Element] =
    findClosest(selector, new Elements(elem)).find(_.isBefore(elem))

  def findClosestAfterOption(selector: String, elem: Element): Option[Element] =
    findClosest(selector, new Elements(elem)).find(_.isAfter(elem))

}

trait ClosestElement extends ElementTarget {

  def closestOption(selector: String): Option[Element] =
    ClosestFinder.findClosestOption(selector, target)

  def closest(selector: String): Elements =
    ClosestFinder.findClosest(selector, new Elements(target))

  def closestBeforeOption(selector: String): Option[Element] =
    ClosestFinder.findClosestBeforeOption(selector, target)

  def closestAfterOption(selector: String): Option[Element] =
    ClosestFinder.findClosestAfterOption(selector, target)

}

trait ClosestElements extends ElementsTarget {

  def closestOption(selector: String): Option[Element] = {
    ClosestFinder.findClosestOption(selector, target)
  }

  def closest(selector: String): Elements = {
    ClosestFinder.findClosest(selector, target)
  }

}