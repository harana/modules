package com.harana.modules.zendesk.models

sealed trait ZendeskError
object ZendeskError {
  case class RateLimit(e: Exception) extends ZendeskError
  case class Response(e: Exception) extends ZendeskError
  case class Unknown(t: Throwable) extends ZendeskError
}