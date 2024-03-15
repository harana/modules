package com.harana.modules.plugin.models

sealed trait PluginError
object PluginError {
  case object NoResourceFound extends PluginError
  case object NoServicesFound extends PluginError
  case object NoServiceFound extends PluginError
  case class Exception(t: Throwable) extends PluginError
}
