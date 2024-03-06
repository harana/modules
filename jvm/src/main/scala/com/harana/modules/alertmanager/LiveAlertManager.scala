package com.harana.modules.alertmanager

import com.harana.modules.core.config.Config
import com.harana.modules.core.http.Http
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.kubernetes.Kubernetes
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import skuber.Container.Port
import skuber.apps.StatefulSet
import skuber.{ConfigMap, Container, EnvVar, HTTPGetAction, LabelSelector, ObjectMeta, PersistentVolume, PersistentVolumeClaim, Pod, Probe, Protocol, Service, Volume}
import zio.{Task, ZIO, ZLayer}

object LiveAlertManager {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      http          <- ZIO.service[Http]
      kubernetes    <- ZIO.service[Kubernetes]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAlertManager(config, http, kubernetes, logger, micrometer)
  }
}

case class LiveAlertManager(config: Config, http: Http, kubernetes: Kubernetes, logger: Logger, micrometer: Micrometer) extends AlertManager {

    override def start(name: String,
                       storageClassName: String,
                       replicas: Int = 1) =
      for {
        version       <- config.string("alertmanager.version")
        config        <- alertManagerConfig(name)

        replicaArgs   =  if (replicas > 1)
                          List(
                            "--cluster.advertise-address=$(POD_IP):9094",
                            "--cluster.listen-address=0.0.0.0:9094"
                          ) ++ (0 to replicas).map(i => s"--cluster.peer=$name-{{ $i }}.$name-headless:9094")
                         else List()

        configMap     =  ConfigMap(name).withData(Map("alertmanager.yml" -> config))
        service       =  Service(name, Map("app.kubernetes.io/name" -> name), 9094)
        probe         =  Probe(HTTPGetAction(port = Right("http"), path = "/"))
        statefulSet   =  StatefulSet(name)
                          .withReplicas(replicas)
                          .withServiceName(name)
                          .withLabelSelector(LabelSelector())
                          .withVolumeClaimTemplate(
                            PersistentVolumeClaim(
                              metadata = ObjectMeta(name = "storage"),
                              spec = Some(PersistentVolumeClaim.Spec(
                                accessModes = List(PersistentVolume.AccessMode.ReadWriteOnce),
                                storageClassName = Some(storageClassName)
                              ))
                            )
                          )
                          .withTemplate(Pod.Template.Spec(
                            metadata = ObjectMeta(labels = Map()),
                            spec = Some(Pod.Spec(
                              nodeSelector = Map("type" -> "core"),
                              containers = List(Container(
                                name = "alertmanager",
                                image = s"quay.io/prometheus/alertmanager:$version",
                                ports = List(Port(9093, Protocol.TCP, "http")),
                                env = List(EnvVar("POD_IP", EnvVar.FieldRef("status.podIP", "v1"))),
                                livenessProbe = Some(probe),
                                readinessProbe = Some(probe),
                                args = replicaArgs ++ List(
                                  "--storage.path=/alertmanager",
                                  "--config.file=/etc/alertmanager/alertmanager.yml"
                                ),
                                volumeMounts = List(
                                  Volume.Mount("config", "/etc/alertmanager"),
                                  Volume.Mount("storage", "/alertmanager")
                                )
                              ))
                            ))
                          ))
      } yield ()


    private def alertManagerConfig(name: String): Task[String] =
      ZIO.attempt("")


    def healthy: Task[Boolean] =
      for {
        domain     <- config.string("alertmanager.host")
        response   <- http.get(s"https://$domain/-/healthy").mapError(ex => new Exception(ex.toString))
      } yield response.code() == 200


    def ready: Task[Boolean] =
      for {
        domain     <- config.string("alertmanager.host")
        response   <- http.get(s"https://$domain/-/ready").mapError(ex => new Exception(ex.toString))
      } yield response.code() == 200


    def reload: Task[Unit] =
      post("-/reload")


    def status: Task[AlertManagerStatus] =
      get[AlertManagerStatus]("api/v2/status")


    def receivers: Task[List[ReceiverName]] =
      get[List[ReceiverName]]("api/v2/receivers")


    def silences(filters: Set[String] = Set()): Task[List[Silence]] =
      get[List[Silence]]("api/v2/silences")


    def silence(id: SilenceId): Task[Silence] =
      get[Silence](s"api/v2/silences/${id.silenceID}")


    def saveSilence(silence: PostableSilence): Task[SilenceId] =
      postWithResponse[PostableSilence, SilenceId]("api/v2/silences", Some(silence))


    def deleteSilence(id: SilenceId): Task[Unit] =
      delete(s"api/v2/silence/${id.silenceID}")


    def alerts(active: Boolean = false,
               silenced: Boolean = false,
               inhibited: Boolean = false,
               unprocessed: Boolean = false,
               filters: List[String] = List(),
               receiver: Option[String] = None): Task[List[Alert]] =
      get[List[Alert]](s"api/v2/alerts",
        Map(
          "active" -> List(active.toString),
          "silenced" -> List(silenced.toString),
          "inhibited" -> List(inhibited.toString),
          "unprocessed" -> List(unprocessed.toString),
          "filters" -> filters) ++ (if (receiver.nonEmpty) Map("receiver" -> List(receiver.get)) else Map())
      )

    def saveAlerts(alerts: List[PostableAlert]): Task[Unit] =
      postWithBody[List[PostableAlert]]("api/v2/alerts", Some(alerts))


    def alertGroups(active: Boolean = false,
                    silenced: Boolean = false,
                    inhibited: Boolean = false,
                    filters: List[String] = List(),
                    receiver: Option[String] = None): Task[List[AlertGroup]] =
      get[List[AlertGroup]]("api/v2/alerts/group",
        Map(
          "active" -> List(active.toString),
          "silenced" -> List(silenced.toString),
          "inhibited" -> List(inhibited.toString),
          "filters" -> filters) ++ (if (receiver.nonEmpty) Map("receiver" -> List(receiver.get)) else Map())
      )


    private def get[A](url: String, parameters: Map[String, List[String]] = Map())(implicit d: Decoder[A]): Task[A] =
      for {
        domain     <- config.string("alertmanager.host")
        json       <- http.getAsJson(s"https://$domain/$url", parameters).mapError(ex => new Exception(ex.toString))
        obj        <- ZIO.fromEither(decode[A](json.noSpaces))
      } yield obj


    private def post(url: String, parameters: Map[String, List[String]] = Map()): Task[Unit] =
      for {
        domain     <- config.string("alertmanager.host")
        _          <- http.postAsJson(s"https://$domain/$url", params = parameters).mapError(ex => new Exception(ex.toString))
      } yield ()


    private def postWithBody[A](url: String, obj: Option[A], parameters: Map[String, List[String]] = Map())(implicit e: Encoder[A]): Task[Unit] =
      for {
        domain     <- config.string("alertmanager.host")
        _          <- http.postAsJson(s"https://$domain/$url", body = obj.map(_.asJson.noSpaces), params = parameters).mapError(ex => new Exception(ex.toString))
      } yield ()


    private def postWithResponse[A, B](url: String, obj: Option[A], parameters: Map[String, List[String]] = Map())(implicit d: Decoder[B], e: Encoder[A]): Task[B] =
      for {
        domain     <- config.string("alertmanager.host")
        json       <- http.postAsJson(s"https://$domain/$url", body = obj.map(_.asJson.noSpaces), params = parameters).mapError(ex => new Exception(ex.toString))
        obj        <- ZIO.fromEither(decode[B](json.noSpaces))
      } yield obj


    private def delete[A](url: String, parameters: Map[String, List[String]] = Map()): Task[Unit] =
      for {
        domain     <- config.string("alertmanager.host")
        _          <- http.deleteAsJson(s"https://$domain/$url", parameters).mapError(ex => new Exception(ex.toString))
      } yield ()
}