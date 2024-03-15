package com.harana.modules.jsoup.models

import org.jsoup.nodes.Element

import scala.annotation.tailrec

trait DocumentPositioning extends ElementTarget {

  lazy val documentCoordinates = coordinatesOf(List(), Some(target)).reverse

  @tailrec
  private def coordinatesOf(accum: List[Int], maybeElement: Option[Element]): List[Int] = {
    if (maybeElement.isEmpty) {
      accum
    } else {
      val elem = maybeElement.get
      coordinatesOf(accum :+ elem.siblingIndex, Option(elem.parent))
    }
  }

  private def compareCoordinates(maybeOther: Option[DocumentPositioning])(f: (Int, Int) => Boolean): Boolean = {
    maybeOther.fold(false) { other =>
      val zip = documentCoordinates.zipAll(other.documentCoordinates, 0, 0)
      val maybeFirstDiff = zip.dropWhile(dc => dc._1 == dc._2).headOption
      maybeFirstDiff.fold(false) { diff =>
        f(diff._1, diff._2)
      }
    }
  }

  def isBefore(other: DocumentPositioning): Boolean =
    compareCoordinates(Option(other))((a, b) => a < b)

  def isAfter(other: DocumentPositioning): Boolean =
    compareCoordinates(Option(other))((a, b) => a > b)
}