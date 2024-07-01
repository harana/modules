package com.harana.modules.kubernetes_s3

import com.harana.web.modules.crud.
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.kubernetes.Kubernetes
import io.circe.syntax._
import skuber.PersistentVolume.AccessMode
import skuber.Resource.Quantity
import skuber.Volume.GenericVolumeSource
import skuber._
import skuber.json.format._
import zio.{ZIO, ZLayer}

object LiveKubernetesS3 {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      kubernetes    <- ZIO.service[Kubernetes]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveKubernetesS3(config, kubernetes, logger, micrometer)
  }
}

case class LiveKubernetesS3(config: Config, kubernetes: Kubernetes, logger: Logger, micrometer: Micrometer) extends KubernetesS3 {

  def createPersistentVolumeClaim(namePrefix: String,
                                  namespace: String,
                                  s3StorageClassName: String,
                                  s3Endpoint: String,
                                  s3Bucket: String,
                                  s3Path: String,
                                  s3AccessKeyId: String,
                                  s3SecretAccessKey: String,
                                  s3Capacity: Int) =
    for {
      _                 <- logger.info(s"Creating volume with prefix: $namePrefix")

      client            <- kubernetes.newClient

      _                 <- kubernetes.create[Secret](client, namespace,
                            Secret(metadata = ObjectMeta(name = s"$namePrefix-secret", namespace = namespace),
                             data = Map("accessKeyID" -> s3AccessKeyId.getBytes, "secretAccessKey" -> s3SecretAccessKey.getBytes, "endpoint" -> s3Endpoint.getBytes)
                           ))

      _                 <- kubernetes.create[PersistentVolume](client, namespace,
                            PersistentVolume(metadata = ObjectMeta(name = s"$namePrefix-pv", namespace = namespace),
                             spec = Some(PersistentVolume.Spec(
                                accessModes = List(AccessMode.ReadWriteMany),
                                capacity = Map(Resource.storage -> Quantity(s"${s3Capacity}Gi")),
                                claimRef = Some(ObjectReference(name = s"$namePrefix-pvc", namespace = namespace)),
//                                  storageClassName = Some(s3StorageClassName),
                                source = GenericVolumeSource(
                                  Map(
                                    "csi" -> Map(
                                      "driver"                      -> s"ru.yandex.s3.csi".asJson,
                                      "controllerPublishSecretRef"  -> Map("name" -> s"$namePrefix-secret", "namespace" -> namespace).asJson,
                                      "nodePublishSecretRef"        -> Map("name" -> s"$namePrefix-secret", "namespace" -> namespace).asJson,
                                      "nodeStageSecretRef"          -> Map("name" -> s"$namePrefix-secret", "namespace" -> namespace).asJson,
                                      "volumeAttributes"            -> Map("capacity" -> s"${s3Capacity}Gi", "mounter" -> "geesfs").asJson,
                                      "volumeHandle"                -> s"$s3Bucket/$s3Path".asJson
                                    ).asJson
                                  ).asJson.noSpaces
                                )
                             ))))

      _                 <- kubernetes.create[PersistentVolumeClaim](client, namespace,
                            PersistentVolumeClaim(metadata = ObjectMeta(name = s"$namePrefix-pvc", namespace = namespace),
                              spec = Some(PersistentVolumeClaim.Spec(
                                accessModes = List(AccessMode.ReadWriteMany),
                                resources = Some(Resource.Requirements(requests = Map(Resource.storage -> Quantity(s"${s3Capacity}Gi"))))
                              ))
                            ))
    } yield s"$namePrefix-pvc"
}