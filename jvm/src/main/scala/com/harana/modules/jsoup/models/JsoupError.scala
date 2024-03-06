package com.harana.modules.jsoup.models

sealed trait JsoupError extends Product with Serializable
object JsoupError {
  case object NotFound extends JsoupError
  case object AlreadyStopped extends JsoupError
  case class Exception(t: Throwable) extends JsoupError
}