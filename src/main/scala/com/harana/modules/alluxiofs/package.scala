package com.harana.modules

import alluxio.AlluxioURI
import alluxio.client.file.FileSystem
import alluxio.conf.{AlluxioProperties, InstancedConfiguration, PropertyKey}
import alluxio.exception.AlluxioException
import zio.{IO, ZIO}

import java.io.{InputStream, OutputStream}

package object alluxiofs {

  def alluxioFs(properties: AlluxioProperties, username: Option[String] = None) =
    ZIO.succeed {
      FileSystem.Factory.create(
        username match {
          case Some(u) =>
            val p = properties.copy()
            p.set(PropertyKey.SECURITY_LOGIN_USERNAME, u)
            new InstancedConfiguration(p)

          case None =>
            new InstancedConfiguration(properties)
        }
      )
    }

  def closeStream(is: InputStream) =
    ZIO.succeed(is.close())

  def closeStream(os: OutputStream) =
    ZIO.succeed(os.close())

  def io[A](fn: => A): IO[AlluxioException, A] =
    ZIO.from(fn).refineToOrDie[AlluxioException]

  def uri(path: String) =
    new AlluxioURI(path)

}
