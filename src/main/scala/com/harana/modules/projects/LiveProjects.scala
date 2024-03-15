package com.harana.modules.projects

import com.harana.designer.backend.modules.projects.models.{Trigger, _}
import com.harana.modules.argo.events.EventSource._
import com.harana.modules.argo.events.Rollout.{BlueGreen, Canary, Rollout, Strategy}
import com.harana.modules.argo.events.Sensor.{EventDependency, Http, K8SResource, Sensor, Subscription, TriggerTemplate}
import com.harana.modules.argo.events.Trigger.{K8SSource, K8STrigger}
import com.harana.modules.argo.events._
import com.harana.modules.argo.workflows._
import com.harana.modules.argo.{EnvironmentVariable, ObjectMetadata, Requests, Resources, VolumeMount, Container => ArgoContainer, Template => ArgoTemplate}
import com.harana.modules.buildpack.Buildpack
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.docker.Docker
import com.harana.modules.git.Git
import com.harana.modules.kubernetes.Kubernetes
import com.harana.modules.vertx.Vertx
import io.circe.yaml.parser
import io.scalaland.chimney.dsl._
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.{Git => JGit}
import zio.{Clock, _}

import java.io.{File => JFile}
import java.nio.charset.Charset
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

object LiveProjects {
  val layer = ZLayer {
    for {
      buildpack     <- ZIO.service[Buildpack]
      config        <- ZIO.service[Config]
      docker        <- ZIO.service[Docker]
      git           <- ZIO.service[Git]
      kubernetes    <- ZIO.service[Kubernetes]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
      vertx         <- ZIO.service[Vertx]
    } yield LiveProjects(buildpack, config, docker, git, kubernetes, logger, micrometer, vertx)
  }
}

case class LiveProjects(buildpack: Buildpack,
                        config: Config,
                        docker: Docker,
                        git: Git,
                        kubernetes: Kubernetes,
                        logger: Logger,
                        micrometer: Micrometer,
                        vertx: Vertx) extends Projects {

  private val projectsRepository = new AtomicReference[JGit]()
  private val allProjects = new AtomicReference[Set[Project]](Set())
  private val allRepositories = new AtomicReference[Map[(Project, String), JGit]](Map())
  private val tempDirectory = Files.createTempDirectory("harana-projects").toFile
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss")

  private val repositoryAuthConfig =
    for {
      username        <- config.optString("projects.docker.repository.username")
      password        <- config.optString("projects.docker.repository.password")
      identityToken   <- config.optString("projects.docker.repository.identityToken")
      registryToken   <- config.optString("projects.docker.repository.registryToken")
      authConfig      =  dockerAuthConfig(username, password, identityToken, registryToken)
    } yield authConfig


  def setup(namespace: String): Task[Unit] =
    for {
      authConfig      <- repositoryAuthConfig

      _               <- logger.info("Creating Argo Events/Workflow CRDs")
      client          <- kubernetes.newClient
      _               <- kubernetes.save(client, namespace, EventSource.crd)
      _               <- kubernetes.save(client, namespace, Rollout.crd)
      _               <- kubernetes.save(client, namespace, Sensor.crd)
      _               <- kubernetes.save(client, namespace, Workflow.crd)
      _               <- kubernetes.save(client, namespace, WorkflowTemplate.crd)
      _               <- kubernetes.close(client)

      _               <- logger.info("Pulling Python/Scala build images")
      pythonImage     <- config.optString("projects.build.pythonImage")
      scalaImage      <- config.optString("projects.build.scalaImage")
      _               <- ZIO.when(pythonImage.nonEmpty)(docker.pullImage(pythonImage.get))
      _               <- ZIO.when(scalaImage.nonEmpty)(docker.pullImage(scalaImage.get, authConfig))

      _               <- logger.info("Setting default Buildpack builder")
      defaultBuilder  <- config.optString("projects.build.buildpack.defaultBuilder")
      _               <- ZIO.when(defaultBuilder.nonEmpty)(buildpack.setDefaultBuilder(defaultBuilder.get))

      _               <- logger.info("Cloning Projects Git repository")
      _               <- cloneProjects

    } yield ()


  def startMonitoring(namespace: String): Task[Unit] =
    for {
      _ <- refreshProjects.repeat(Schedule.spaced(5.seconds))
    } yield ()


  def stopMonitoring(namespace: String): Task[Unit] = {
    ZIO.unit
  }

  private def cloneProjects: Task[Unit] =
    for {
      url             <- config.string("projects.git.url")
      branch          <- config.string("projects.git.branch", "master")
      username        <- config.optString("projects.git.username")
      password        <- config.optString("projects.git.password")
      oauthToken      <- config.optString("projects.git.oauthToken")
      gitRepository   <- git.clone(url, tempDirectory, Some(branch), username, password, oauthToken)
      _               =  projectsRepository.set(gitRepository)
    } yield ()


  private def refreshProjects: Task[Set[Project]] =
    for {
      hasChanged  <- git.hasChanged(projectsRepository.get)
      _           <- logger.debug(s"Refreshing Projects Git repository: ${if (hasChanged) "changed" else "not changed" }")
      projects    <- ZIO.ifZIO(ZIO.succeed(hasChanged))(changedProjects(projectsRepository.get), ZIO.attempt(Set[Project]()))
    } yield projects


  private def cloneRepository(project: Project, repository: Repository): Task[Unit] =
    for {
      branchTagOrCommit   <- ZIO.succeed(repository.git.branch.orElse(repository.git.tag.orElse(repository.git.commit.orElse(None))))
      directory           =  new JFile(tempDirectory, s"$project/${repository.name}")
      gitRepository       <- git.clone(repository.git.url, directory, None, repository.git.username, repository.git.password, repository.git.oauthToken)
      _                   <- git.checkout(gitRepository, branchTagOrCommit.getOrElse("master"))
      _                   =  allRepositories.set(allRepositories.get + ((project, repository.name) -> gitRepository))
    } yield ()


  private def changedProjects(projectsRepo: JGit): Task[Set[Project]] =
    for {
      foundProjects     <- findProjects(projectsRepo.getRepository.getDirectory.getParentFile)
      changedProjects   =  allProjects.get.filterNot(foundProjects)
      _                 <- logger.debug(s"Changed projects: ${changedProjects.map(_.title).mkString(", ")}").when(changedProjects.nonEmpty)
    } yield changedProjects


  private def findProjects(directory: JFile): Task[Set[Project]] =
    for {
      files               <- ZIO.succeed(FileUtils.listFiles(directory, Array("yml"), true).asScala.toList)
      _                   <- logger.debug(s"Found files: ${files.map(_.getAbsolutePath).mkString(", ")}")
      ymls                <- ZIO.attempt(files.map(f => (f.getName, FileUtils.readFileToString(f, Charset.defaultCharset()))))
      parsedProjects      <- ZIO.foreach(ymls)(parseProject)
      foundProjects       =  parsedProjects.filter(_.nonEmpty).map(_.get).toSet
      _                   <- logger.info(s"Found projects: ${foundProjects.map(_.title)}")
    } yield foundProjects


  private def parseProject(yml: (String, String)): UIO[Option[Project]] =
    for {
      json        <- ZIO.succeed(parser.parse(yml._2))
      _           <- ZIO.when(json.isLeft)(logger.error(s"Failed to parse YAML: ${yml._1} due to error: ${json.left.get.getMessage}"))
      _           <- ZIO.when(json.isRight && json.toOption.get.as[Project].isLeft)(logger.error(s"Failed to parse project: ${yml._1} due to error: ${json.toOption.get.as[Project].left.get.getMessage}"))
      project     =  json.toOption.flatMap(_.as[Project].toOption)
    } yield project


  private def refreshRepositories(project: Project): Task[Set[Repository]] =
    project.repositories match {
      case Some(repositories) =>
        for {
          _                 <- logger.debug(s"Refreshing repositories for project: ${project.title}")
          changedReposRef   <- Ref.make(Set[Repository]())
          _                 <- ZIO.foreach(repositories) { r =>
                                if (allRepositories.get.contains((project, r.name)))
                                  for {
                                    gitRepo       <- ZIO.succeed(allRepositories.get()((project, r.name)))
                                    hasChanged    <- git.hasChanged(gitRepo)
                                    _             <- changedReposRef.getAndUpdate(_ + r).when(hasChanged)
                                  } yield ()
                                else
                                  cloneRepository(project, r) *> changedReposRef.getAndUpdate(_ + r)
          }
          changedRepos      <- changedReposRef.get
        } yield changedRepos
      case None => ZIO.attempt(Set())
    }


  private def buildContainers(project: Project): Task[List[Boolean]] =
    for {
      _               <- logger.debug(s"Building containers for project: ${project.title}")
      successes       <- ZIO.foreachPar(project.containers) { c =>
                          val imageName = s"${name(project.title)}_${name(c.name)}"
                          val date = dateFormatter.format(new Date())

                          if (c.auto.map(_.repository).nonEmpty)
                            for {
                              git         <- ZIO.succeed(allRepositories.get()((project, c.docker.get.repository.get)))
                              success     <- buildpack.build(s"$imageName:$date", git.getRepository.getDirectory).map(_.mkString(",").contains("ERROR"))
                            } yield success

                          if (c.docker.map(_.repository).nonEmpty)
                            for {
                              git         <- ZIO.succeed(allRepositories.get()((project, c.docker.get.repository.get)))
                              dockerFile  =  new JFile(git.getRepository.getDirectory, c.docker.get.path.getOrElse("Dockerfile"))
                              success     <- docker.buildImage(dockerFile, Set(date)).option.map(_.nonEmpty)
                            } yield success

                          ZIO.succeed(false)
      }
    } yield successes


  private def pipelineTriggers(project: Project): UIO[List[(Pipeline, List[Trigger])]] =
    ZIO.succeed(project.pipelines.map(_.map(p => (p, p.start.triggers.getOrElse(List())))).getOrElse(List()))


  private def eventSources(eventBusName: String, project: Project): UIO[List[EventSource]] =
    for {
      triggers      <- pipelineTriggers(project).map(_.flatMap(_._2))
      eventSources  <- ZIO.foreach(triggers) { trigger =>
        for {
          eventSourceName   <- ZIO.succeed(s"${name(project.title)}-${name(trigger.name)}-eventsource")
          spec              =  EventSource.Spec(
                                calendar = trigger.calendar.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                eventBusName = eventBusName,
                                file = trigger.file.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                github = trigger.github.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                gitlab = trigger.gitlab.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                hdfs = trigger.hdfs.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                kafka = trigger.kafka.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                redis = trigger.redis.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                resource = trigger.resource.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                slack = trigger.slack.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                sns = trigger.sns.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                sqs = trigger.sqs.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                stripe = trigger.stripe.map(c => Map(trigger.name -> c)).getOrElse(Map()),
                                webhook = trigger.webhook.map(c => Map(trigger.name -> c)).getOrElse(Map())
                               )
          eventSource       =  EventSource(eventSourceName, spec)
        } yield eventSource
      }
    } yield eventSources


  private def rollouts(project: Project): UIO[List[Rollout]] =
    project.daemons match {
      case Some(daemons) => ZIO.foreach(daemons) { daemon =>
        for {
          strategy    <- ZIO.succeed(daemon.strategy.map(s => Strategy(s.blueGreen.map(_.into[BlueGreen].transform), s.canary.map(_.into[Canary].transform))))
          containers  <- ZIO.foreach(daemon.containers)(argoContainer(project, _))
          spec        =  Rollout.Spec(minReadySeconds = daemon.minReadySeconds, replicas = daemon.replicas, revisionHistoryLimit = daemon.revisionHistoryLimit, strategy = strategy)
          rollout     =  Rollout(daemon.name, spec)
        } yield rollout
      }
      case None => ZIO.succeed(List.empty)
    }


  private def sensors(project: Project): UIO[List[Sensor]] =
    for {
      pipelineTriggers      <- pipelineTriggers(project).map(pt => pt.filter(_._2.isEmpty))
      argoContainer         =  ArgoContainer(
                                  name = "sensor",
                                  image = "argoproj/sensor:v0.13.0",
                                  imagePullPolicy = Some("Always")
                                )
      template              =  ArgoTemplate(container = Some(argoContainer))
      sensors               <- ZIO.foreach(pipelineTriggers){ pipelineTrigger =>
                                for {
                                  prefix        <- ZIO.succeed(s"${name(project.title)}-${name(pipelineTrigger._1.name)}")
                                  dependencies  =  pipelineTrigger._2.map(t => EventDependency(s"${name(t.name)}-gateway", s"${name(project.title)}-${name(t.name)}-gateway", "example"))
                                  subscription  =  Subscription(Some(Http(9300)))
                                  metadata      =  ObjectMetadata(generateName = Some(s"$prefix-workflow-"))
                                  workflow      <- argoWorkflow(project, pipelineTrigger._1)
                                  k8sResource   =  K8SResource("argoproj.io/v1alpha1", "Workflow", metadata, workflow)
                                  k8sTrigger    =  K8STrigger("argoproj.io", "v1alpha1", "workflows", "create", K8SSource(k8sResource))
                                  triggers      =  List(Sensor.Trigger(template = TriggerTemplate(s"$prefix-workflow", k8s = Some(k8sTrigger))))
                                  sensor        =  Sensor(s"$prefix-workflow", Sensor.Spec(Some(template), dependencies, subscription = Some(subscription), triggers = triggers))
                                } yield sensor
      }
    } yield sensors


  private def argoWorkflow(project: Project, pipeline: Pipeline): UIO[Workflow.Spec] =
    for {
      entrypoint        <- ZIO.succeed(pipeline.start.action)
      containers        <- ZIO.foreach(pipeline.actions.map(_.container))(argoContainer(project, _))
      templates         =  containers.map(c => Template(container = Some(c), name = c.name))
      dagTasks          =  pipeline.actions.map(a => DAGTask(name = Some(a.name), dependencies = a.dependencies.getOrElse(List()), template = Some(a.name)))
      dagTemplate       =  Template(name = pipeline.name, dag = Some(DAG(tasks = dagTasks)))
      workflow          =  Workflow.Spec(entrypoint = Some(entrypoint), templates = templates :+ dagTemplate)
    } yield workflow


  private def name(title: String) =
    title.toLowerCase.replaceAll("\\s", "-")


  private def argoContainer(project: Project, container: Container): UIO[ArgoContainer] =
    for {
      globalContainer       <- ZIO.succeed(project.containers.filter(_.name.equals(container.name)).head)
      args                  =  container.arguments.getOrElse(globalContainer.arguments.getOrElse(List()))
      command               =  container.command.getOrElse(globalContainer.command.getOrElse(List()))
      environmentVariables  =  container.environmentVariables.map(_ ++ globalContainer.environmentVariables.getOrElse(List()))
      envs                  =  environmentVariables.getOrElse(List()).map(e => EnvironmentVariable(e.name, e.value))
      image                 =  globalContainer.docker.flatMap(_.image).getOrElse(s"${globalContainer.name}:latest")
      imagePullPolicy       =  container.imagePullPolicy.orElse(globalContainer.imagePullPolicy)
      resources             =  Resources(container.resources.orElse(globalContainer.resources).map(_.into[Requests].transform))
      volumeMounts          =  container.volumeMounts.getOrElse(globalContainer.volumeMounts.getOrElse(List())).map(_.into[VolumeMount].transform)
      argoContainer         =  ArgoContainer(args, command, envs, image, imagePullPolicy, None, container.name, Some(resources), volumeMounts)
    } yield argoContainer


  private def projectDeployed(eventBusName: String, namespace: String, project: Project): Task[Boolean] =
    for {
      client          <- kubernetes.newClient
      e               <- customResourcesDeployed[EventSource](client, namespace, eventSources(eventBusName, project))(kubernetes)
      r               <- customResourcesDeployed[Rollout](client, namespace, rollouts(project))(kubernetes)
      s               <- customResourcesDeployed[Sensor](client, namespace, sensors(project))(kubernetes)
      deployed        =  e && r && s
      _               <- kubernetes.close(client)
    } yield deployed


  private def deployProject(eventBusName: String, namespace: String, project: Project): Task[Boolean] =
    for {
      _               <- logger.debug(s"Deploying project: ${project.title} to namespace: $namespace")
      client          <- kubernetes.newClient
      e               <- deployCustomResources[EventSource](client, namespace, eventSources(eventBusName, project))(kubernetes)
      r               <- deployCustomResources[Rollout](client, namespace, rollouts(project))(kubernetes)
      s               <- deployCustomResources[Sensor](client, namespace, sensors(project))(kubernetes)
      deployed        =  e && r && s
      _               <- kubernetes.close(client)
    } yield deployed


  private def undeployProject(eventBusName: String, namespace: String, project: Project): Task[Boolean] =
    for {
      _               <- logger.debug(s"Un-deploying project: ${project.title} from namespace: $namespace")
      client          <- kubernetes.newClient
      e               <- undeployCustomResources[EventSource](client, namespace, eventSources(eventBusName, project))(kubernetes)
      r               <- undeployCustomResources[Rollout](client, namespace, rollouts(project))(kubernetes)
      s               <- undeployCustomResources[Sensor](client, namespace, sensors(project))(kubernetes)
      undeployed      =  e && r && s
      _               <- kubernetes.close(client)
    } yield undeployed
}