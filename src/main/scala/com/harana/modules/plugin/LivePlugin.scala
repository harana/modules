package com.harana.modules.plugin

import com.harana.models.{PluginInfo, PluginServiceInfo}
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.ohc.OHC
import com.harana.modules.plugin.LivePlugin._
import com.harana.modules.plugin.models.PluginError
import com.harana.sdk.shared.plugin.Service
import com.harana.sdk.shared.plugin.Service.{ServiceId => PluginServiceId}
import org.apache.felix.framework.Felix
import org.osgi.framework.Constants
import zio._

import java.io.File
import java.net.URL
import java.util
import scala.reflect.ClassTag

object LivePlugin {

  val scalazVersion = "7.2.7"
  val scalaVersion = scala.util.Properties.versionNumberString

  val systemPackages =
    "scala;version=" + scalaVersion +
      ",scala.annotation;version=" + scalaVersion +
      ",scala.collection;version=" + scalaVersion +
      ",scala.collection.convert;version=" + scalaVersion +
      ",scala.collection.generic;version=" + scalaVersion +
      ",scala.collection.immutable;version=" + scalaVersion +
      ",scala.collection.mutable;version=" + scalaVersion +
      ",scala.collection.parallel;version=" + scalaVersion +
      ",scala.collection.parallel.immutable;version=" + scalaVersion +
      ",scala.collection.parallel.mutable;version=" + scalaVersion +
      ",scala.collection.script;version=" + scalaVersion +
      ",scala.concurrent;version=" + scalaVersion +
      ",scala.concurrent.duration;version=" + scalaVersion +
      ",scala.io;version=" + scalaVersion +
      ",scala.math;version=" + scalaVersion +
      ",scala.reflect.api;version=" + scalaVersion +
      ",scala.reflect.internal;version=" + scalaVersion +
      ",scala.reflect.internal.util;version=" + scalaVersion +
      ",scala.reflect;version=" + scalaVersion +
      ",scala.reflect.macros.blackbox;version=" + scalaVersion +
      ",scala.reflect.macros.whitebox;version=" + scalaVersion +
      ",scala.reflect.macros.contexts;version=" + scalaVersion +
      ",scala.reflect.macros;version=" + scalaVersion +
      ",scala.runtime;version=" + scalaVersion +
      ",scala.runtime.java8;version=" + scalaVersion +
      ",scala.tools.nsc;version=" + scalaVersion +
      ",scala.tools.nsc.ast;version=" + scalaVersion +
      ",scala.tools.nsc.typechecker;version=" + scalaVersion +
      ",scala.sys;version=" + scalaVersion +
      ",scala.util;version=" + scalaVersion +
      ",scala.util.control;version=" + scalaVersion +
      ",scala.util.hashing;version=" + scalaVersion +
      ",scala.annotation;version=" + scalaVersion +
      ",scala.util.matching;version=" + scalaVersion +
      ",scala.xml;version=1.0.6" +
      ",scalaz;version=" + scalazVersion +
      ",scalaz.syntax;version=" + scalazVersion +
      ",org.osgi.framework;version=1.6.0" +
      ",org.osgi.service,services,utils,com.fasterxml.jackson,play,io.gatling.jsonpath,android.util,org.joda.convert,org.apache.felix.scr,org.slf4j,io.dropwizard.metrics.healthchecks"

  val sdkPackages =
    "com.harana.sdk.components" +
      ",com.harana.sdk.components.basic" +
      ",com.harana.sdk.components.cards" +
      ",com.harana.sdk.components.elements" +
      ",com.harana.sdk.components.lists" +
      ",com.harana.sdk.components.maps" +
      ",com.harana.sdk.components.panels" +
      ",com.harana.sdk.components.structure" +
      ",com.harana.sdk.components.widgets" +
      ",com.harana.sdk.models" +
      ",com.harana.sdk.parameters" +
      ",com.harana.sdk.plugin"

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LivePlugin(config, logger, micrometer)
  }
}

case class LivePlugin(config: Config, logger: Logger, micrometer: Micrometer) extends Plugin {

  private val bundlesDirectory = config.string("plugins.bundleDir").map(f => new File(f))
  private val pluginsDirectory = config.string("plugins.pluginsDir").map(f => new File(f))
  private val cacheDirectory = config.string("plugins.cacheDir").map(f => new File(f))

  private val bundleContext =
    for {
      bundlesDir <- bundlesDirectory
      pluginsDir <- pluginsDirectory
      props <- ZIO.succeed {
        val props = new util.HashMap[String, String]
        props.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, systemPackages + "," + sdkPackages)
        props.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT)
        props.put(Constants.FRAMEWORK_STORAGE, cacheDirectory.toString)
        props.put("felix.shutdown.hook", "true")
        props.put("felix.service.urlhandlers", "true")
        props.put("felix.fileinstall.dir", pluginsDir.getAbsolutePath)
        props.put("felix.fileinstall.noInitialDelay", "true")
        props.put("felix.fileinstall.log.level", "4")
        props
      }
      felix   =  new Felix(props)
      _       <- ZIO.attempt(felix.start()).mapError(PluginError.Exception)
      _       <- installSystemBundles(bundlesDir, felix.getBundleContext)
      _       <- ZIO.attempt(felix.init()).mapError(PluginError.Exception)
  } yield felix.getBundleContext


  def findPlugins[T <: Service](implicit cmf: ClassTag[T]): IO[PluginError, List[PluginInfo]] =
    for {
      context   <- bundleContext
      plugins   =  context.getBundles.map(b => PluginInfo(b.getSymbolicName, "", b.getVersion.getMajor.toLong)).toList
    } yield plugins


  def findServices[T <: Service](implicit cmf: ClassTag[T]): IO[PluginError, Map[PluginServiceId, T]] =
    for {
      context     <- bundleContext
      references  =  context.getAllServiceReferences(cmf.runtimeClass.getName, null)
      services    <- {
                    if (references == null) ZIO.fail(PluginError.NoServicesFound)
                    else ZIO.succeed(
                      references.map { ref =>
                        val id = ref.getProperty("id").asInstanceOf[PluginServiceId]
                        val service = context.getService(ref).asInstanceOf[T]
                        (id, service)
                      }.toMap
                    )
                  }
    } yield services


  def findServiceInfos[T <: Service](implicit cmf: ClassTag[T]): IO[PluginError, List[PluginServiceInfo]] =
    for {
      services      <- findServices
      serviceInfos  =  services.map { case (serviceId, any) => PluginServiceInfo()}.toList
    } yield serviceInfos


  def getService[T <: Service](serviceId: PluginServiceId)(implicit cmf: ClassTag[T]): IO[PluginError, T] =
    for {
      services  <- findServices(cmf)
      service   <- services.get(serviceId) match {
                    case Some(x) => ZIO.succeed(x)
                    case None => ZIO.fail(PluginError.NoServiceFound)
                  }
    } yield service


  def getResource(className: String, resourcePath: String): IO[PluginError, URL] =
    for {
      context   <- bundleContext
      resource  <- context.getBundles
                  .find(_.getEntry(className.replace(".", "/") + ".class") != null)
                  .map(_.getEntry(resourcePath)) match {
                    case Some(x) => ZIO.succeed(x)
                    case None => ZIO.fail(PluginError.NoResourceFound)
                  }
    } yield resource
}