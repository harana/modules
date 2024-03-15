package com.harana.modules.sentry

import io.sentry.Breadcrumb
import zio.UIO
import zio.macros.accessible

@accessible
trait Sentry {
  def addBreadcrumb(message: String): UIO[Unit]
  def addBreadcrumb(breadcrumb: Breadcrumb): UIO[Unit]
}