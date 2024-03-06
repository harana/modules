package com.harana.modules.auth0.models

sealed trait Auth0Error
object Auth0Error {
  case class Api(e: Exception) extends Auth0Error
  case class RateLimit(e: Exception) extends Auth0Error
  case class Request(e: Exception) extends Auth0Error
}