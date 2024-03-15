package com.harana.modules.zookeeper

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.docker.Docker
import com.harana.modules.zookeeper.LiveZookeeper.image
import zio.{Task, ZIO, ZLayer}

object LiveZookeeper {
  private val image = "zookeeper:3.6"

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      docker        <- ZIO.service[Docker]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveZookeeper(config,docker, logger, micrometer)
  }
}

case class LiveZookeeper(config: Config, docker: Docker, logger: Logger, micrometer: Micrometer) extends Zookeeper {

  def localStart: Task[Unit] =
    for {
      _           <- logger.info("Starting Zookeeper")
      running     <- docker.containerRunning("zookeeper")
      _           <- logger.debug("Existing Zookeeper container not found. Starting a new one.").when(!running)
      _           <- start.when(!running)
    } yield ()

  def localStop: Task[Unit] =
    for {
      _           <- logger.info("Stopping Zookeeper")
      containers  <- docker.listContainers(nameFilter = List("zookeeper"))
      _           <- ZIO.foreach(containers.map(_.getId))(id => docker.stopContainer(id))
    } yield ()

  private def start =
    for {
      _           <- docker.pullImage(image)
      id          <- docker.createContainer("zookeeper", image, exposedPorts = Map(2181 -> 2181))
      _           <- docker.startContainer(id)
    } yield ()

}