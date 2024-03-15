package com.harana.modules.buildpack

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.process.Command
import zio.{Task, ZIO, ZLayer}

import java.io.File
import java.util.Locale
import scala.collection.mutable

object LiveBuildpack {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveBuildpack(config, logger, micrometer)
  }
}

case class LiveBuildpack(config: Config, logger: Logger, micrometer: Micrometer) extends Buildpack {

  private val buildpackCmd = ZIO.attempt {
    val os = System.getProperty("os.name").toLowerCase(Locale.ROOT)
    val url = {
      if (os.contains("mac")) getClass.getResource("pack/mac/pack")
      if (os.contains("win")) getClass.getResource("pack/windows/pack.exe")
      getClass.getResource("pack/linux/pack")
    }
    url.getFile
  }


  def build(name: String,
            path: File,
            builder: Option[String] = None,
            environmentVariables: Map[String, String] = Map(),
            mountedVolumes: Map[File, File] = Map(),
            network: Option[String] = None,
            publish: Option[Boolean] = None,
            runImage: Option[String] = None): Task[List[String]] =
    for {
      cmd <- buildpackCmd
      args <- ZIO.succeed {
        val args = mutable.ListBuffer[String]("build", "name", s"--path ${path.getAbsolutePath}")
        if (builder.nonEmpty) args += s"--builder ${builder.get}"
        if (environmentVariables.nonEmpty) args += s"--env ${environmentVariables.map { case (k,v) => s"$k=$v" }.mkString(",")}"
        if (mountedVolumes.nonEmpty) args += s"--volume ${mountedVolumes.map { case (k,v) => s"$k:$v" }.mkString(",")}"
        if (network.nonEmpty) args += s"--network ${network.get}"
        if (publish.nonEmpty) args += s"--publish"
        if (runImage.nonEmpty) args += s"--run-image ${runImage.get}s"
        args
      }
      cmd <- Command(cmd, args.toSeq: _*).lines
    } yield cmd.toList


  def setDefaultBuilder(name: String): Task[List[String]] =
    Command("pack", List("set-default-builder", name): _*).lines.map(_.toList)


  def rebase(name: String,
             publish: Option[Boolean] = None,
             runImage: Option[String] = None): Task[List[String]] =
    for {
      cmd <- buildpackCmd
      args <- ZIO.succeed {
        val args = mutable.ListBuffer[String]()
        if (publish.nonEmpty) args += s"--publish"
        if (runImage.nonEmpty) args += s"--run-image ${runImage.get}s"
        args
      }
      cmd <- Command(cmd, args.toSeq: _*).lines
    } yield cmd.toList
}