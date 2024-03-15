package com.harana.modules.docker

import com.github.dockerjava.api.command._
import com.github.dockerjava.api.exception.{DockerException, NotFoundException, UnauthorizedException}
import com.github.dockerjava.api.model.Network.Ipam
import com.github.dockerjava.api.model.{Service => DockerService, _}
import com.harana.modules.core.http.models.OkHttpError
import zio.macros.accessible
import zio.{IO, Queue, UIO}

import java.io.{File, InputStream}

@accessible
trait Docker {

    def attachContainer(id: ContainerId): IO[DockerException, InputStream]

    def auth(username: Option[String], password: Option[String], identityToken: Option[String], registryToken: Option[String]): IO[UnauthorizedException, String]

    def auth(config: Option[AuthConfig]): IO[UnauthorizedException, String]

    def authConfig(username: Option[String], password: Option[String], identityToken: Option[String], registryToken: Option[String]): UIO[AuthConfig]

    def buildImage(dockerFileOrFolder: File, tags: Set[String]): IO[DockerException, ImageId]

    def buildImage(inputStream: InputStream, tags: Set[String]): IO[DockerException, ImageId]

    def commit(id: ContainerId): IO[NotFoundException, String]

    def connectToNetwork: UIO[Unit]

    def containerDiff(id: ContainerId): IO[NotFoundException, List[ChangeLog]]

    def containerExists(containerName: String): UIO[Boolean]

    def containerNotExists(containerName: String): UIO[Boolean]

    def containerRunning(containerName: String): UIO[Boolean]

    def containerNotRunning(containerName: String): UIO[Boolean]

    def copyResourceFromContainer(id: ContainerId, resource: String, hostPath: Option[String] = None): IO[NotFoundException, InputStream]

    def copyArchiveToContainer(id: ContainerId, tarInputStream: InputStream, remotePath: Option[String] = None): IO[NotFoundException, Unit]

    def copyResourceToContainer(id: ContainerId, resource: String, remotePath: Option[String] = None): IO[NotFoundException, Unit]

    def createContainer(name: String,
                        imageName: String,
                        command: Option[String] = None,
                        exposedPorts: Map[Int, Int] = Map()): IO[DockerException, ContainerId]

    def createImage(repository: String, imageStream: InputStream): IO[NotFoundException, ImageId]

    def createNetwork(name: Option[String] = None,
                      attachable: Boolean = false,
                      checkDuplicate: Boolean = false,
                      driver: Option[String] = None,
                      enableIpv6: Boolean = false,
                      internal: Boolean = false,
                      ipam: Option[Ipam] = None,
                      labels: Map[String, String] = Map(),
                      options: Map[String, String] = Map()): IO[DockerException, NetworkId]

    def createService(spec: ServiceSpec): IO[NotFoundException, ServiceId]

    def createVolume(name: String, driver: Option[String] = None, driverOpts: Map[String, String] = Map()): IO[NotFoundException, String]

    def disconnectFromNetwork(networkId: Option[NetworkId] = None, containerId: Option[ContainerId] = None, force: Boolean = false): UIO[Unit]

    def ensureContainerIsRunning(name: String, imageName: String, command: Option[String] = None, exposedPorts: Map[Int, Int] = Map()): IO[DockerException, Unit]

    def ensureLocalRegistryIsRunning: IO[DockerException, Unit]

    def events(containerFilter: List[ContainerId] = List(),
               eventFilter: List[EventId] = List(),
               imageFilter: List[ImageId] = List(),
               labelFilter: Map[String, String] = Map(),
               withSince: Option[String] = None,
               withUntil: Option[String] = None): UIO[Queue[Event]]

    def execCreate(id: ExecId,
                   attachStderr: Boolean = false,
                   attachStdin: Boolean = false,
                   attachStdout: Boolean = false,
                   cmd: List[String] = List(),
                   containerId: Option[ContainerId] = None,
                   env: List[String] = List(),
                   privileged: Boolean = false,
                   tty: Boolean = false,
                   user: Option[String] = None,
                   workingDir: Option[String] = None): IO[NotFoundException, String]

    def execStart(id: ExecId,
                  detach: Boolean = false,
                  stdIn: Option[InputStream] = None,
                  tty: Boolean = false): UIO[Queue[Frame]]

    def hubTags(namespace: String,
                repository: String,
                page: Option[Int] = None,
                pageSize: Option[Int] = None): zio.Task[List[HubTag]]

    def info: UIO[Info]

    def initializeSwarm(spec: SwarmSpec): UIO[Unit]

    def inspectContainer(id: ContainerId): IO[NotFoundException, InspectContainerResponse]

    def inspectExec(id: ExecId): IO[NotFoundException, InspectExecResponse]

    def inspectImage(id: ImageId): IO[NotFoundException, InspectImageResponse]

    def inspectNetwork(id: Option[NetworkId] = None): IO[NotFoundException, Network]

    def inspectService(id: ServiceId): IO[NotFoundException, DockerService]

    def inspectSwarm: IO[NotFoundException, Swarm]

    def inspectVolume(name: String): IO[NotFoundException, InspectVolumeResponse]

    def joinSwarm(advertiseAddr: Option[String] = None,
                  joinToken: Option[String] = None,
                  listenAddr: Option[String] = None,
                  remoteAddrs: List[String] = List()): UIO[Unit]

    def killContainer(id: ContainerId, signal: Option[String] = None): IO[NotFoundException, Unit]

    //    def launchListenContainer: IO[DockerException, List[String]]

    def leaveSwarm(force: Boolean = false): UIO[Unit]

    def listArtifactoryRepositories(registryUrl: String,
                                    repository: String,
                                    authConfig: AuthConfig): IO[OkHttpError, List[String]]

    def listArtifactoryTags(registryUrl: String,
                            repository: String,
                            authConfig: AuthConfig,
                            image: String,
                            maximum: Option[Int] = None): IO[OkHttpError, List[String]]

    def listContainers(ancestorFilter: List[String] = List(),
                       before: Option[String] = None,
                       exitedFilter: Option[Int] = None,
                       filters: Map[String, List[String]] = Map(),
                       idFilter: List[String] = List(),
                       labelFilter: Map[String, String] = Map(),
                       limit: Option[Int] = None,
                       nameFilter: List[String] = List(),
                       networkFilter: List[String] = List(),
                       showAll: Option[Boolean] = None,
                       showSize: Option[Boolean] = None,
                       since: Option[String] = None,
                       statusFilter: List[String] = List(),
                       volumeFilter: List[String] = List()): UIO[List[Container]]

    def listDockerRepositories(registryUrl: String): IO[OkHttpError, List[String]]

    def listDockerTags(registryUrl: String,
                       image: String,
                       maximum: Option[Int] = None): IO[OkHttpError, List[String]]

    def listImages(danglingFilter: Option[Boolean] = None,
                   imageNameFilter: Option[String] = None,
                   labelFilter: Map[String, String] = Map(),
                   showAll: Option[Boolean] = None): UIO[List[Image]]

    def listNetworks(filter: Option[(String, List[String])] = None,
                     idFilter: List[String] = List(),
                     nameFilter: List[String] = List()): IO[NotFoundException, List[Network]]

    def listServices(idFilter: List[String] = List(),
                     labelFilter: Map[String, String] = Map(),
                     nameFilter: List[String] = List()): IO[NotFoundException, List[DockerService]]

    def listSwarmNodes(idFilter: List[String] = List(),
                       membershipFilter: List[String] = List(),
                       nameFilter: List[String] = List(),
                       roleFilter: List[String] = List()): IO[NotFoundException, List[SwarmNode]]

    def listTasks(idFilter: List[String] = List(),
                  labelFilter: Map[String, String] = Map(),
                  nameFilter: List[String] = List(),
                  nodeFilter: List[String] = List(),
                  serviceFilter: List[String] = List(),
                  stateFilter: List[TaskState] = List()): IO[NotFoundException, List[Task]]

    def listVolumes(includeDangling: Boolean = true, filter: Option[(String, List[String])] = None) : IO[NotFoundException, List[InspectVolumeResponse]]

    def loadImage(stream: InputStream): UIO[Unit]

    def logContainer(id: ContainerId,
                     followStream: Option[Boolean] = None,
                     since: Option[Int] = None,
                     stdErr: Option[Boolean] = None,
                     stdOut: Option[Boolean] = None,
                     tail: Option[Int] = None,
                     timestamps: Option[Boolean] = None): UIO[Queue[Frame]]

    def logService(id: ServiceId,
                   details: Option[Boolean] = None,
                   follow: Option[Boolean] = None,
                   since: Option[Int] = None,
                   stdout: Option[Boolean] = None,
                   stderr: Option[Boolean] = None,
                   tail: Option[Int] = None,
                   timestamps: Option[Boolean] = None): UIO[Queue[Frame]]

    def logTask(id: ServiceId,
                details: Option[Boolean] = None,
                follow: Option[Boolean] = None,
                since: Option[Int] = None,
                stdout: Option[Boolean] = None,
                stderr: Option[Boolean] = None,
                tail: Option[Int] = None,
                timestamps: Option[Boolean] = None): UIO[Queue[Frame]]

    def pauseContainer(id: ContainerId): IO[NotFoundException, Unit]

    def ping: UIO[Unit]

    def prune(pruneType: PruneType,
              dangling: Option[Boolean] = None,
              labelFilter: List[String] = List(),
              untilFilter: Option[String] = None): IO[NotFoundException, Long]

    def pullImage(repository: String,
                  authConfig: Option[AuthConfig] = None,
                  platform: Option[String] = None,
                  registry: Option[String] = None,
                  tag: Option[String] = None): IO[DockerException, Unit]

    def pushImage(name: String,
                  authConfig: Option[AuthConfig] = None,
                  tag: Option[String] = None): IO[DockerException, Unit]

    def removeContainer(id: ContainerId, force: Boolean = false, removeVolumes: Boolean = false): IO[NotFoundException, Unit]

    def removeContainers(name: String, force: Boolean = false, removeVolumes: Boolean = false): IO[NotFoundException, Unit]

    def removeImage(id: ImageId, force: Boolean = false, prune: Boolean = true): IO[NotFoundException, Unit]

    def removeNetwork(id: NetworkId): IO[NotFoundException, Unit]

    def removeService(id: ServiceId): IO[NotFoundException, Unit]

    def removeVolume(name: String): IO[NotFoundException, Unit]

    def renameContainer(id: ContainerId, name: String): UIO[Unit]

    def restartContainer(id: ContainerId, timeout: Option[Int] = None): IO[DockerException, Unit]

    def saveImage(name: String, tag: Option[String] = None): IO[NotFoundException, InputStream]

    def searchImages(term: String): UIO[List[SearchItem]]

    def startContainer(id: ContainerId): IO[DockerException, Unit]


    def startLocalRegistry: IO[DockerException, Unit]

    def stats(id: ContainerId): IO[DockerException, Statistics]

    def stopContainer(id: ContainerId, timeout: Option[Int] = None): IO[DockerException, Unit]

    def stopLocalRegistry: IO[DockerException, Unit]

    def tagImage(id: ImageId, imageNameWithRepository: String, tag: String, force: Boolean = false): UIO[Unit]

    def topContainer(id: ContainerId, psArgs: Option[String] = None): IO[NotFoundException, TopContainerResponse]

    def unpauseContainer(id: ContainerId): IO[NotFoundException, Unit]

    def updateContainer(id: ContainerId,
                        blkioWeight: Option[Int] = None,
                        cpuPeriod: Option[Int] = None,
                        cpuQuota: Option[Int] = None,
                        cpusetCpus: Option[String] = None,
                        cpusetMems: Option[String] = None,
                        cpuShares: Option[Int] = None,
                        kernelMemory: Option[Long] = None,
                        memory: Option[Long] = None,
                        memoryReservation: Option[Long] = None,
                        memorySwap: Option[Long] = None): IO[NotFoundException, UpdateContainerResponse]

    def updateService(id: ServiceId, spec: ServiceSpec): UIO[Unit]

    def updateSwarm(spec: SwarmSpec): UIO[Unit]

    def updateSwarmNode(id: SwarmId, spec: SwarmNodeSpec, version: Option[Long] = None): IO[NotFoundException, Unit]

    def version: UIO[Unit]

    def waitForContainer(id: ContainerId): IO[DockerException, Int]

}