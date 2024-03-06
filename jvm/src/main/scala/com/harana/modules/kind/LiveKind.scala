package com.harana.modules.kind

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.kind.models.Cluster
import zio.process.Command
import zio.{Task, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable

object LiveKind {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveKind(config, logger, micrometer)
  }
}

case class LiveKind(config: Config, logger: Logger, micrometer: Micrometer) extends Kind {

  def createCluster(name: String,
                    cluster: Option[Cluster] = None,
                    kubeConfig: Option[File] = None,
                    nodeImage: Option[String] = None,
                    retainNodesOnFailure: Boolean = false,
                    waitForControlPlane: Int = 0) =
    for {
      _ <- logger.info(s"Creating Kind cluster: $name")
      args <- ZIO.succeed {
        val args = mutable.ListBuffer[String]("create", "cluster", "--name", name)
        if (cluster.nonEmpty) args += s"--config ${generateConfig(cluster.get)}"
        if (kubeConfig.nonEmpty) args += s"--kubeconfig ${kubeConfig.get.getAbsolutePath}"
        if (nodeImage.nonEmpty) args += s"--image ${nodeImage.get}"
        if (retainNodesOnFailure) args += s"--retain ${retainNodesOnFailure.toString}"
        if (waitForControlPlane > 0) args += s"--wait ${waitForControlPlane}s"
        args
      }
      cmd <- Command("kind", args.toSeq: _*).lines
    } yield cmd.toList


  def deleteCluster(name: String, kubeConfig: Option[File] = None): Task[Unit] =
    for {
      _ <- logger.info(s"Deleting Kind cluster: $name")
      args <- ZIO.succeed {
        val args = mutable.ListBuffer[String]("delete", "cluster", "--name", name)
        if (kubeConfig.nonEmpty) args += s"--kubeconfig ${kubeConfig.get.getAbsolutePath}"
        args
      }
      _ <- Command("kind", args.toSeq: _*).lines
    } yield ()


  def listClusters: Task[List[String]] =
    for {
      cmd <- Command("kind", "list", "nodes").lines
    } yield cmd.toList


  def listNodes(name: String): Task[List[String]] =
    for {
      cmd <- Command("kind", List("--name", name): _*).lines
    } yield cmd.toList


  def buildBaseImage(image: String): Task[Unit] =
    null


  def buildNodeImage(image: String): Task[Unit] =
    null


  def loadImage(image: String): Task[Unit] =
    null


  def exportLogs(path: Option[File] = None): Task[Unit] =
    null


  def exportKubeConfig(name: String,
                       path: Option[File] = None): Task[Unit] =
    null


  def printKubeConfig(name: String,
                      internalAddress: Boolean = false): Task[Unit] =
    null
}