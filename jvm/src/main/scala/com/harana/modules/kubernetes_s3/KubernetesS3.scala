package com.harana.modules.kubernetes_s3

import zio.Task
import zio.macros.accessible

@accessible
trait KubernetesS3 {

    def createPersistentVolumeClaim(namePrefix: String,
                                    namespace: String,
                                    s3StorageClassName: String,
                                    s3Endpoint: String,
                                    s3Bucket: String,
                                    s3Path: String,
                                    s3AccessKeyId: String,
                                    s3SecretAccessKey: String,
                                    s3Capacity: Int): Task[String]
}