package com.harana.modules.buildpack

import zio.Task
import zio.macros.accessible

import java.io.File

@accessible
trait Buildpack {

    def build(name: String,
              path: File,
              builder: Option[String] = None,
              environmentVariables: Map[String, String] = Map(),
              mountedVolumes: Map[File, File] = Map(),
              network: Option[String] = None,
              publish: Option[Boolean] = None,
              runImage: Option[String] = None): Task[List[String]]

    def setDefaultBuilder(name: String): Task[List[String]]

    def rebase(name: String,
               publish: Option[Boolean] = None,
               runImage: Option[String] = None): Task[List[String]]

}

object Buildpack {
  type ContainerId = String
}