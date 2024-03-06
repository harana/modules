package com.harana.modules.ignite

import zio.Task
import zio.macros.accessible

@accessible
trait Ignite {

    def attach(vmId: String,
               quiet: Option[Boolean] = None): Task[List[String]]

    def completion(quiet: Option[Boolean] = None): Task[List[String]]

    def copyToVM(source: String,
                 destination: String,
                 quiet: Option[Boolean] = None,
                 timeout: Option[Int] = None): Task[List[String]]

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
                 volumes: List[String] = List()): Task[List[String]]

    def exec(vmId: String,
             command: String,
             quiet: Option[Boolean] = None,
             timeout: Option[Int] = None,
             tty: Option[Boolean] = None): Task[List[String]]

    def importImage(ociImage: String,
                    quiet: Option[Boolean] = None): Task[List[String]]

    def inspectVM(vmId: String,
                  outputFormat: Option[String] = None,
                  quiet: Option[Boolean] = None,
                  template: Option[String] = None): Task[List[String]]

    def killVMs(vmIds: List[String],
                quiet: Option[Boolean] = None): Task[List[String]]

    def listImages(quiet: Option[Boolean] = None): Task[List[String]]

    def listVMs(all: Option[Boolean] = None,
                filter: Option[String] = None,
                quiet: Option[Boolean] = None,
                template: Option[String] = None):  Task[List[String]]

    def logs(vmId: String,
             quiet: Option[Boolean] = None): Task[List[String]]

    def removeImages(imageIds: List[String],
                     force: Option[Boolean] = None,
                     quiet: Option[Boolean] = None): Task[List[String]]

    def removeVMs(vmIds: List[String],
                  force: Option[Boolean] = None,
                  quiet: Option[Boolean] = None): Task[List[String]]

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
              volumes: List[String] = List()): Task[List[String]]

    def ssh(vmId: String,
            quiet: Option[Boolean] = None,
            timeout: Option[Int] = None,
            tty: Option[Boolean] = None): Task[List[String]]

    def startVM(vmId: String,
                interactive: Option[Boolean] = None,
                quiet: Option[Boolean] = None): Task[List[String]]

    def stopVM(vmId: List[String],
               force: Option[Boolean] = None,
               quiet: Option[Boolean] = None): Task[List[String]]

    def version(outputFormat: Option[String] = None,
                quiet: Option[Boolean] = None): Task[List[String]]
}