package com.harana.modules.aws_s3

import io.vertx.core.buffer.Buffer
import io.vertx.ext.reactivestreams.{ReactiveReadStream, ReactiveWriteStream}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model._
import zio.Task
import zio.macros.accessible

import java.time.Instant

@accessible
trait AwsS3 {

    def newClient(credentials: AwsCredentialsProvider,
              region: Option[Region] = None,
              endpoint: Option[String] = None,
              targetThroughput: Option[Double] = None): Task[S3AsyncClient]

    def createBucket(client: S3AsyncClient, bucket: String): Task[Unit]

    def deleteBucket(client: S3AsyncClient, bucket: String): Task[Unit]

    def listBuckets(client: S3AsyncClient): Task[List[Bucket]]

    def bucketExists(client: S3AsyncClient, bucket: String): Task[Boolean]

    def getBucketPolicy(client: S3AsyncClient, bucket: String): Task[String]

    def getBucketAcl(client: S3AsyncClient, bucket: String): Task[GetBucketAclResponse]

    def putBucketAcl(client: S3AsyncClient, bucket: String, acl: BucketCannedACL): Task[Unit]

    def listObjects(client: S3AsyncClient, bucket: String, prefix: Option[String] = None): Task[ListObjectsV2Response]

    def deleteObject(client: S3AsyncClient, bucket: String, key: String): Task[Unit]

    def deleteObjects(client: S3AsyncClient, bucket: String, identifiers: List[ObjectIdentifier]): Task[Unit]

    def getObject(client: S3AsyncClient,
                  bucket: String,
                  key: String,
                  ifMatch: Option[String] = None,
                  ifNoneMatch: Option[String] = None,
                  ifModifiedSince: Option[Instant] = None,
                  ifUnmodifiedSince: Option[Instant] = None,
                  range: Option[String] = None): Task[(GetObjectResponse, ReactiveReadStream[Buffer])]

    def putObject(client: S3AsyncClient,
                  bucket: String,
                  key: String,
                  writeStream: ReactiveWriteStream[Buffer],
                  acl: ObjectCannedACL,
                  contentLength: Option[Long] = None,
                  contentMD5: Option[String] = None,
                  storageClass: Option[String] = None,
                  tags: Map[String, String] = Map()): Task[String]

    def copyObject(client: S3AsyncClient,
                   sourceBucket: String,
                   sourceKey: String,
                   destinationBucket: String,
                   destinationKey: String,
                   sourceIfMatch: Option[String] = None,
                   sourceIfNoneMatch: Option[String] = None,
                   sourceIfModifiedSince: Option[Instant] = None,
                   sourceIfUnmodifiedSince: Option[Instant] = None): Task[CopyObjectResult]

    def getObjectAttributes(client: S3AsyncClient, bucket: String, key: String): Task[GetObjectAttributesResponse]

    def getObjectTagging(client: S3AsyncClient, bucket: String, key: String): Task[GetObjectTaggingResponse]

    def getObjectAcl(client: S3AsyncClient, bucket: String, key: String): Task[GetObjectAclResponse]

    def putObjectAcl(client: S3AsyncClient, bucket: String, key: String, acl: ObjectCannedACL): Task[Unit]

    def putObjectTagging(client: S3AsyncClient, bucket: String, key: String, tag: Map[String, String]): Task[Unit]

    def uploadPartCopy(client: S3AsyncClient,
                       sourceBucket: String,
                       sourceKey: String,
                       destinationBucket: String,
                       destinationKey: String,
                       uploadId: String,
                       partNumber: Int,
                       copySourceIfMatch: Option[String],
                       copySourceIfNoneMatch: Option[String],
                       copySourceIfModifiedSince: Option[Instant],
                       copySourceIfUnmodifiedSince: Option[Instant],
                       copySourceRange: Option[String]): Task[CopyPartResult]

    def uploadPart(client: S3AsyncClient,
                   bucket: String,
                   key: String,
                   uploadId: String,
                   partNumber: Int,
                   writeStream: ReactiveWriteStream[Buffer],
                   contentLength: Option[Long] = None): Task[String]

    def listParts(client: S3AsyncClient, bucket: String, key: String, uploadId: String): Task[List[Part]]

    def listMultipartUploads(client: S3AsyncClient, bucket: String, prefix: Option[String] = None): Task[List[MultipartUpload]]

    def createMultipartUpload(client: S3AsyncClient, bucket: String, key: String, cannedACL: ObjectCannedACL): Task[String]

    def abortMultipartUpload(client: S3AsyncClient, bucket: String, key: String, uploadId: String): Task[Unit]

    def completeMultipartUpload(client: S3AsyncClient, bucket: String, key: String, uploadId: String): Task[String]

    def presignedUrl(bucketName: String, key: String, expirationMinutes: Int): Task[String]

}