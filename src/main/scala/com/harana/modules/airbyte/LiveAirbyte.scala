package com.harana.modules.airbyte

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.kubernetes.Kubernetes
import io.airbyte.protocol.models.{AirbyteCatalog, AirbyteConnectionStatus, ConfiguredAirbyteCatalog}
import io.circe.parser._
import skuber._
import zio.{Task, ZIO, ZLayer}

object LiveAirbyte {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      kubernetes    <- ZIO.service[Kubernetes]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAirbyte(config, kubernetes, logger, micrometer)
  }
}

case class LiveAirbyte(config: Config, kubernetes: Kubernetes, logger: Logger, micrometer: Micrometer) extends Airbyte {

    def integrations: Task[List[AirbyteIntegration]] =
      for {
        files         <- ZIO.succeed(airbyteFiles)
        jsons         =  files.view.mapValues(parse).filter(_._2.isRight).mapValues(_.toOption.get)
        integrations  =  jsons.map { j => toAirbyteIntegration(j._1, j._2)}.toList
      } yield integrations


    def check(integrationName: String, connectionValues: Map[String, Object]): Task[AirbyteConnectionStatus] =
      null

    def discover(integrationName: String, connectionValues: Map[String, Object]): Task[AirbyteCatalog] =
      null


    def read(integrationName: String, catalog: ConfiguredAirbyteCatalog): Task[Unit] =
      null


    private def run(integrationName: String,
                    prefix: String,
                    namespace: String,
                    s3StorageClassName: String,
                    s3Endpoint: String,
                    s3Bucket: String,
                    s3Path: String,
                    s3AccessKeyId: String,
                    s3SecretAccessKey: String) =
      for {
        client            <- kubernetes.newClient

        secret            =  Secret(metadata = ObjectMeta(name = s"$prefix-secret", namespace = namespace),
                               data = Map("accessKeyID" -> s3AccessKeyId.getBytes, "secretAccessKey" -> s3SecretAccessKey.getBytes, "endpoint" -> s3Endpoint.getBytes)
                             )

//        pv                =  PersistentVolume(metadata = ObjectMeta(name = s"$prefix-volume", namespace = namespace),
//                               spec = Some(PersistentVolume.Spec(
//                                  accessModes = List(AccessMode.ReadWriteMany),
//                                  capacity = Map(Resource.storage -> Quantity("10Gi")),
//                                  claimRef = Some(ObjectReference(name = s"$prefix-pvc", namespace = namespace)),
//                                  storageClassName = Some(ObjectReference(name = "csi-s3")),
//                                  storageClassName = Some("csi-s3"),
//                                  source = GenericVolumeSource(
//                                    Map(
//                                      "csi" -> Map(
//                                        "driver"                      -> s"ru.yandex.s3.csi".asJson,
//                                        "controllerPublishSecretRef"  -> Map("name" -> "csi-s3-secret", "namespace" -> namespace).asJson,
//                                        "nodePublishSecretRef"        -> Map("name" -> "csi-s3-secret", "namespace" -> namespace).asJson,
//                                        "nodeStageSecretRef"          -> Map("name" -> "csi-s3-secret", "namespace" -> namespace).asJson,
//                                        "volumeAttributes"            -> Map("capacity" -> "10Gi", "mounter" -> "geesfs").asJson,
//                                        "volumeHandle"                -> s"$s3Bucket/$s3Path".asJson
//                                      ).asJson
//                                    ).asJson.noSpaces
//                                  )
//                               ))
//                             )
//
//        pvc               =  PersistentVolumeClaim(metadata = ObjectMeta(name = s"$prefix-volume", namespace = namespace),
//                              spec = Some(PersistentVolumeClaim.Spec(
//                                accessModes = List(AccessMode.ReadWriteMany),
//                                resources = Some(Resource.Requirements(requests = Map(Resource.storage -> Quantity("10Gi"))))
//                              ))
//                             )
//
//        volumeMount       =  Volume.Mount(name = "user-home", mountPath = "/home/harana", subPath = claims.userId)
//        containerSpec     =  Container(name = name, image = app.image, imagePullPolicy = Some(Container.PullPolicy.Always), volumeMounts = List(volumeMount)).exposePort(app.httpPort)
//        volume            =  Volume(name = "user-home", Volume.PersistentVolumeClaimRef("user-home"))
//
//        podSpec           =  Pod.Spec(imagePullSecrets = List(LocalObjectReference("aws-registry")))
//        pod               =  Pod(name = podName, spec = podSpec)
//        _                 <- kubernetes.create(client, namespace, pod).ignore
//        _                 <- kubernetes.create(client, podNamespace, pod).ignore

      } yield ()
}