package com.harana.modules.vertx.models

import com.google.common.base.{Joiner, Splitter}
import com.harana.modules.vertx.models.CrossOriginResourceSharing._

import java.util.regex.Pattern
import scala.jdk.CollectionConverters._

case class CrossOriginResourceSharing(allowedOrigins: Set[String],
                                      allowedMethods: Set[String],
                                      allowedHeaders: Set[String]) {

  val allowedHeadersRaw = Joiner.on(HEADER_VALUE_SEPARATOR).join(allowedHeaders.asJava)
  val allowedMethodsRaw = Joiner.on(HEADER_VALUE_SEPARATOR).join(allowedMethods.asJava)
  val anyOriginAllowed = allowedOrigins.contains(ALLOW_ANY_ORIGIN)
  val allowedOriginPatterns = allowedOrigins.filterNot(_.equals(ALLOW_ANY_ORIGIN)).map(Pattern.compile(_, Pattern.CASE_INSENSITIVE))

  def getAllowedMethods =
    allowedMethodsRaw

  def getAllowedOrigin(origin: String) =
    if (anyOriginAllowed) ALLOW_ANY_ORIGIN else origin

  def isOriginAllowed(origin: String): Boolean =
    anyOriginAllowed || allowedOriginPatterns.forall(_.matcher(origin).matches)

  def isMethodAllowed(method: String) =
    allowedMethods.contains(method)

  def isEveryHeaderAllowed(headers: String) =
    allowedHeadersRaw.equals(ALLOW_ANY_HEADER) || Splitter.on(HEADER_VALUE_SEPARATOR).split(headers).asScala.forall(allowedHeaders.contains)

  override def equals(obj: Any): Boolean = {
    if (this == obj)  return true
    if (obj == null || !obj.isInstanceOf[CrossOriginResourceSharing]) return false

    val that = obj.asInstanceOf[CrossOriginResourceSharing]
    this.allowedOrigins.equals(that.allowedOrigins) &&
      this.allowedMethodsRaw.equals(that.allowedMethodsRaw) &&
      this.allowedHeadersRaw.equals(that.allowedHeadersRaw)
  }
}

object CrossOriginResourceSharing {
  val SUPPORTED_METHODS = Set("GET", "HEAD", "PUT", "POST")
  val HEADER_VALUE_SEPARATOR = ", "
  val ALLOW_ANY_ORIGIN = "*"
  val ALLOW_ANY_HEADER = "*"

  def apply() =
    new CrossOriginResourceSharing(Set(ALLOW_ANY_ORIGIN), SUPPORTED_METHODS, Set(ALLOW_ANY_HEADER))
}