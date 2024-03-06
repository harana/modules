package com.harana.modules

import com.harana.modules.plugin.models.PluginError
import org.osgi.framework.BundleContext
import zio.{IO, ZIO}

import java.io.File
import java.nio.file.{Files, Path, Paths}

package object plugin {

  def installPlugin(bundleContext: BundleContext, bundleLocation: String): IO[PluginError, Unit] =
    ZIO.attempt {
      bundleContext.installBundle(bundleLocation).start()
    }.mapError(PluginError.Exception)

  def installSystemBundles(bundlesDirectory: File, bundleContext: BundleContext): IO[PluginError, Unit] =
    for {
      files     <- ZIO.succeed(bundlesDirectory.listFiles.filter(_.isFile).filter(_.getName.endsWith("jar")))
      bundles   <- ZIO.attempt(files.map(b => bundleContext.installBundle(s"file:${b.getAbsolutePath}"))).mapError(PluginError.Exception)
      _         <- ZIO.attempt(bundles.foreach(_.start())).mapError(PluginError.Exception)
    } yield ()

  def uninstallPlugin(bundleContext: BundleContext, bundleLocation: String): IO[PluginError, Unit] =
    ZIO.attempt {
      bundleContext.getBundles
        .filter(_.getLocation == bundleLocation)
        .foreach { bundle =>
          bundle.uninstall()
        }
    }.mapError(PluginError.Exception)

  def removePlugin(pluginsDirectory: File, pluginName: String): IO[PluginError, Unit] =
    ZIO.attempt {
      pluginsDirectory.listFiles
        .filter(_.isFile)
        .filter(_.getName == pluginName)
        .foreach(file => if (file.exists()) file.delete())
    }.mapError(PluginError.Exception)

  def copyPlugin(pluginsDirectory: File, filePath: String): IO[PluginError, Path] =
    ZIO.attempt {
      Files.copy(Paths.get(filePath), Paths.get(pluginsDirectory + "/" + new File(filePath).getName))
    }.mapError(PluginError.Exception)
}