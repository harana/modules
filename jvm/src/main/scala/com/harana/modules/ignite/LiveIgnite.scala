package com.harana.modules.ignite

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.process.Command
import zio.{Task, UIO, ZIO, ZLayer}

object LiveIgnite {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveIgnite(config, logger, micrometer)
  }
}

case class LiveIgnite(config: Config, logger: Logger, micrometer: Micrometer) extends Ignite {

  def attach(vmId: String, quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet           <- booleanArg("quiet", quiet)
      cmd             <- Command("ignite", List("action", vmId) ++ quiet: _*).lines
    } yield cmd.toList


  def completion(quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet           <- booleanArg("quiet", quiet)
      cmd             <- Command("ignite", List("completion") ++ quiet: _*).lines
    } yield cmd.toList


  def copyToVM(source: String,
               destination: String,
               quiet: Option[Boolean] = None,
               timeout: Option[Int] = None): Task[List[String]] =
    for {
      quiet           <- booleanArg("quiet", quiet)
      timeout         <- intArg("timeout", timeout)
      cmd             <- Command("ignite", List("cp", source, destination) ++ quiet ++ timeout: _*).lines
    } yield cmd.toList


  def createVM(ociImage: String,
               copyFiles: List[String] = List(),
               cpus: Option[Int] = None,
               disk: Option[Int] = None,
               labels: Map[String, String] = Map(),
               memory: Option[Int] = None,
               name: Option[String] = None,
               ports: Map[String, String] = Map(),
               quiet: Option[Boolean] = None,
               ssh: Option[String] = None,
               volumes: List[String] = List()): Task[List[String]] =
    for {
      cpusArg         <- intArg("cpus", cpus, includeValue = true)
      diskArg         <- intArg("size", disk, includeValue = true)
      labelsArg       =  if (labels.isEmpty) List() else List("l", labels.map { case (k,v) => s"$k=$v" }.mkString)
      memoryArg       <- intArg("memory", memory, includeValue = true)
      nameArg         <- stringArg("name", name, includeValue = true)
      quietArg        <- booleanArg("quiet", quiet)
      sshArg          =  ssh.map(s => List(s"ssh=$s")).getOrElse(List())
      args            =  cpusArg ++ diskArg ++ labelsArg ++ memoryArg ++ nameArg ++ quietArg ++ sshArg
      cmd             <- Command("ignite", List("create", ociImage) ++ args.toSeq: _*).lines
    } yield cmd.toList


  def exec(vmId: String,
           command: String,
           quiet: Option[Boolean] = None,
           timeout: Option[Int] = None,
           tty: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet         <- booleanArg("quiet", quiet)
      timeout       <- intArg("timeout", timeout)
      tty           <- booleanArg("tty", tty)
      cmd           <- Command("ignite", List("exec", command) ++ quiet ++ timeout ++ tty: _*).lines
    } yield cmd.toList


  def importImage(ociImage: String,
                  quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("image", "import", ociImage) ++ quiet: _*).lines
    } yield cmd.toList


  def inspectVM(vmId: String,
                outputFormat: Option[String] = None,
                quiet: Option[Boolean] = None,
                template: Option[String] = None): Task[List[String]] =
    for {
      outputFormat  <- stringArg("output", outputFormat)
      quiet         <- booleanArg("quiet", quiet)
      template      <- stringArg("t", template, includeValue = true)
      cmd           <- Command("ignite", List("inspect", "vm", vmId) ++ outputFormat ++ quiet ++ template: _*).lines
    } yield cmd.toList


  def killVMs(vmIds: List[String],
              quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("kill") ++ vmIds ++ quiet: _*).lines
    } yield cmd.toList


  def listImages(quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("image", "ls") ++ quiet: _*).lines
    } yield cmd.toList


  def listVMs(all: Option[Boolean] = None,
              filter: Option[String] = None,
              quiet: Option[Boolean] = None,
              template: Option[String] = None):  Task[List[String]] =
    for {
      all           <- booleanArg("all", all)
      filter        <- stringArg("filter", filter, includeValue = true)
      quiet         <- booleanArg("quiet", quiet)
      template      <- stringArg("template", template)
      cmd           <- Command("ignite", List("ps") ++ all ++ filter ++ quiet ++ template: _*).lines
    } yield cmd.toList


  def logs(vmId: String,
           quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("logs", vmId) ++ quiet: _*).lines
    } yield cmd.toList


  def removeImages(imageIds: List[String],
                   force: Option[Boolean] = None,
                   quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      force         <- booleanArg("force", force)
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("rmi") ++ imageIds ++ force ++ quiet: _*).lines
    } yield cmd.toList


  def removeVMs(vmIds: List[String],
                force: Option[Boolean] = None,
                quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      force         <- booleanArg("force", force)
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("rm") ++ vmIds ++ force ++ quiet: _*).lines
    } yield cmd.toList


  def runVM(ociImage: String,
            copyFiles: List[String] = List(),
            cpus: Option[Int] = None,
            disk: Option[Int] = None,
            interactive: Option[Boolean] = None,
            labels: Map[String, String] = Map(),
            memory: Option[Int] = None,
            name: Option[String] = None,
            ports: Map[String, String] = Map(),
            quiet: Option[Boolean] = None,
            ssh: Option[String] = None,
            volumes: List[String] = List()): Task[List[String]] =
    for {
      cpusArg           <- intArg("cpus", cpus, includeValue = true)
      copyFilesArg      =  if (copyFiles.isEmpty) List() else List("copy-files", copyFiles.mkString)
      diskArg           <- intArg("size", disk, includeValue = true)
      interactiveArg    <- booleanArg("i", interactive)
      labelsArg         =  if (labels.isEmpty) List() else List("l", labels.map { case (k,v) => s"$k=$v" }.mkString)
      memoryArg         <- intArg("memory", memory, includeValue = true)
      nameArg           <- stringArg("name", name, includeValue = true)
      quietArg          <- booleanArg("quiet", quiet)
      sshesArg          =  ssh.map(s => List(s"ssh=$s")).getOrElse(List())
      args              =  copyFilesArg ++ cpusArg ++ diskArg ++ interactiveArg ++ labelsArg ++ memoryArg ++ nameArg ++ quietArg ++ sshesArg
      cmd               <- Command("ignite", List("run", ociImage) ++ args.toSeq: _*).lines
    } yield cmd.toList


  def ssh(vmId: String,
          quiet: Option[Boolean] = None,
          timeout: Option[Int] = None,
          tty: Option[Boolean] = None): Task[List[String]] =
    for {
      quiet         <- booleanArg("quiet", quiet)
      timeout       <- intArg("timeout", timeout, includeValue = true)
      tty           <- booleanArg("tty", tty)
      cmd           <- Command("ignite", List("ssh", vmId) ++ quiet ++ timeout ++ tty: _*).lines
    } yield cmd.toList


  def startVM(vmId: String,
              interactive: Option[Boolean] = None,
              quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      interactive   <- booleanArg("interactive", interactive)
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("start", vmId) ++ interactive ++ quiet: _*).lines
    } yield cmd.toList


  def stopVM(vmId: List[String],
             force: Option[Boolean] = None,
             quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      force         <- booleanArg("force-kill", force)
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("stop") ++ vmId ++ force ++ quiet: _*).lines
    } yield cmd.toList


  def version(outputFormat: Option[String] = None,
              quiet: Option[Boolean] = None): Task[List[String]] =
    for {
      outputFormat  <- stringArg("output", outputFormat, includeValue = true)
      quiet         <- booleanArg("quiet", quiet)
      cmd           <- Command("ignite", List("version") ++ outputFormat ++ quiet: _*).lines
    } yield cmd.toList


  private def booleanArg(name: String, arg: Option[Boolean], includeValue: Boolean = false): UIO[List[String]] =
    ZIO.succeed(arg.map(v => List(name) ++ (if (includeValue) List(v.toString) else List())).getOrElse(List()))

  private def intArg(name: String, arg: Option[Int], includeValue: Boolean = false): UIO[List[String]] =
    ZIO.succeed(arg.map(v => List(name) ++ (if (includeValue) List(v.toString) else List())).getOrElse(List()))

  private def stringArg(name: String, arg: Option[String], includeValue: Boolean = false): UIO[List[String]] =
    ZIO.succeed(arg.map(v => List(name) ++ (if (includeValue) List(v) else List())).getOrElse(List()))
}