package com.harana.modules.alluxiofs

import alluxio.client.file.URIStatus
import alluxio.conf.{Configuration, PropertyKey}
import alluxio.grpc.{CreateDirectoryPOptions, DeletePOptions, SetAclAction}
import alluxio.security.authorization.AclEntry
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.sdk.shared.models.HaranaFile
import org.apache.commons.io.IOUtils
import zio.{Task, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object LiveAlluxioFs {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAlluxioFs(config, logger, micrometer)
  }
}

case class LiveAlluxioFs(config: Config, logger: Logger, micrometer: Micrometer) extends AlluxioFs {

    private val alluxioProperties = for {
      hosts           <- config.listString("alluxio.hosts", List())
      port            <- config.long("alluxio.port", 19998)
      properties      =  Configuration.global().copyProperties()
      addresses       =  hosts.map(host => s"$host:$port").mkString(",")
      _               =  properties.set(PropertyKey.SECURITY_AUTHENTICATION_TYPE, "NOSASL")
      _               =  properties.set(PropertyKey.MASTER_RPC_ADDRESSES, addresses)
      _               <- logger.info(s"Connecting to hosts: $addresses")
    } yield properties


    def createDirectory(path: String,
                        createParent: Boolean,
                        username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        options       =  CreateDirectoryPOptions.newBuilder().setRecursive(createParent).build()
        _             <- ZIO.attempt(fs.createDirectory(uri(path), options))
      } yield ()

// FIXME
//    def createFile(path: String,
//                   data: Array[Byte],
//                   username: Option[String] = None,
//                   blockSize: Option[Int] = None): Task[Unit] =
//      for {
//        properties    <- alluxioProperties
//        fs            <- alluxioFs(properties, username)
//        _             <- ZIO.attempt(fs.createFile(uri(path))).acquireReleaseWith(closeStream)(os => io(os.write(data))
//        )
//      } yield ()


    def delete(path: String,
               recursive: Boolean,
               username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        options       =  DeletePOptions.newBuilder().setRecursive(true).build()
        _             <- ZIO.attempt(fs.delete(uri(path), options))
      } yield ()


    def exists(path: String,
               username: Option[String] = None): Task[Boolean] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        result        <- ZIO.attempt(fs.exists(uri(path)))
      } yield result


    def free(path: String,
             username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        result        <- ZIO.attempt(fs.free(uri(path)))
      } yield result


    def info(path: String,
             username: Option[String] = None): Task[HaranaFile] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        result        <- ZIO.attempt(fs.listStatus(uri(path)).asScala.map(toDataFile).head)
      } yield result


    def isDirectory(path: String,
                    username: Option[String] = None): Task[Boolean] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        result        <- ZIO.attempt(fs.getStatus(uri(path)).isFolder)
      } yield result


    def isFile(path: String, username: Option[String] = None): Task[Boolean] =
      isDirectory(path).map(!_)


    def list(path: String,
             username: Option[String] = None): Task[List[HaranaFile]] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        result        <- ZIO.attempt(fs.listStatus(uri(path)).asScala.toList.map(toDataFile))
      } yield result


// FIXME
//    def loadFile(path: String,
//                 username: Option[String] = None): Task[Array[Byte]] =
//      for {
//        properties    <- alluxioProperties
//        fs            <- alluxioFs(properties, username)
//        result        <- ZIO.attempt(fs.openFile(uri(path))).acquireReleaseWithAuto(closeStream)(is => io(IOUtils.toByteArray(is)))
//      } yield result


    def mount(path: String,
              ufsPath: String,
              username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        _             <- ZIO.attempt(fs.mount(uri(path), uri(ufsPath)))
      } yield ()


    def parent(path: String,
               username: Option[String] = None): Task[Option[String]] =
      io(Option(uri(path).getParent).map(_.getPath))


    def persist(path: String,
                username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        _             <- ZIO.attempt(fs.persist(uri(path)))
      } yield ()


    def rename(source: String,
               destination: String,
               username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        _             <- ZIO.attempt(fs.rename(uri(source), uri(destination)))
      } yield ()


    def search(path: String,
               query: String): Task[List[HaranaFile]] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties)
//        _             <- ZIO.from(fs.rename(uri(source), uri(destination)))
      } yield List()


    def setAcl(path: String,
               action: SetAclAction,
               entries: List[AclEntry],
               username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        _             <- ZIO.attempt(fs.setAcl(uri(path), action, entries.asJava))
      } yield ()


    def unmount(path: String,
                username: Option[String] = None): Task[Unit] =
      for {
        properties    <- alluxioProperties
        fs            <- alluxioFs(properties, username)
        _             <- ZIO.attempt(fs.unmount(uri(path)))
      } yield ()


    private def toDataFile(uri: URIStatus): HaranaFile = {
      null
    }
      //    private def toDataFile(uri: URIStatus) = {
//      uri.
//
//      DataFile(
//        name = file.getName.getBaseName,
//        extension = if (StringUtils.isEmpty(file.getName.getExtension)) None else Some(file.getName.getExtension),
//        isFolder = file.isFolder,
//        lastModified = Instant.ofEpochMilli(file.getContent.getLastModifiedTime),
//        size = size(file),
//        tags = List()
//      )
//    }
}