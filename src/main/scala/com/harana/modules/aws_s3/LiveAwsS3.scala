package com.harana.modules.aws_s3

import com.amazonaws.services.s3.internal.ServiceUtils
import com.harana.modules.aws_s3.LiveAwsS3.presigner
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import io.vertx.core.buffer.Buffer
import io.vertx.ext.reactivestreams.{ReactiveReadStream, ReactiveWriteStream}
import org.reactivestreams.{Subscriber, Subscription}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.async.{AsyncRequestBody, AsyncResponseTransformer, SdkPublisher}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model._
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import zio.{Task, ZIO, ZLayer}

import java.net.URI
import java.nio.ByteBuffer
import java.time.{Duration, Instant}
import java.util.Optional
import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters._

object LiveAwsS3 {
  val presigner = S3Presigner.create()

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAwsS3(config, logger, micrometer)
  }
}

case class LiveAwsS3(config: Config, logger: Logger, micrometer: Micrometer) extends AwsS3 {

  def newClient(credentials: AwsCredentialsProvider,
                region: Option[Region] = None,
                endpoint: Option[String] = None,
                targetThroughput: Option[Double] = None): Task[S3AsyncClient] =
    for {
      defaultRegion     <- config.string("aws.defaultRegion")
      clientBuilder     =  S3AsyncClient.crtBuilder()
                            .credentialsProvider(credentials)
                            .region(region.getOrElse(Region.of(defaultRegion)))
                            .targetThroughputInGbps(java.lang.Double.valueOf(targetThroughput.getOrElse(40.0)))
                            .minimumPartSizeInBytes(8 * 1024 * 1024)
      client            =  if (endpoint.nonEmpty) clientBuilder.endpointOverride(URI.create(endpoint.get)).build() else clientBuilder.build()
    } yield client

  def createBucket(client: S3AsyncClient, bucket: String) =
    ZIO.fromCompletableFuture(client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())).unit

  def deleteBucket(client: S3AsyncClient, bucket: String) =
    ZIO.fromCompletableFuture(client.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build())).unit

  def listBuckets(client: S3AsyncClient) =
    ZIO.fromCompletableFuture(client.listBuckets(ListBucketsRequest.builder().build())).map(_.buckets().asScala.toList)

  def bucketExists(client: S3AsyncClient, bucket: String) =
    listBuckets(client).map(b => b.map(_.name()).contains(bucket))

  def getBucketPolicy(client: S3AsyncClient, bucket: String) =
    ZIO.fromCompletableFuture(client.getBucketPolicy(GetBucketPolicyRequest.builder().bucket(bucket).build())).map(_.policy())

  def getBucketAcl(client: S3AsyncClient, bucket: String) =
    ZIO.fromCompletableFuture(client.getBucketAcl(GetBucketAclRequest.builder().bucket(bucket).build()))

  def putBucketAcl(client: S3AsyncClient, bucket: String, acl: BucketCannedACL) =
    ZIO.fromCompletableFuture(client.putBucketAcl(PutBucketAclRequest.builder().bucket(bucket).acl(acl).build())).unit

  def listObjects(client: S3AsyncClient, bucket: String, prefix: Option[String] = None) = {
    var builder = ListObjectsV2Request.builder().bucket(bucket)
    builder = if (prefix.nonEmpty) builder.prefix(prefix.get) else builder
    ZIO.fromCompletableFuture(client.listObjectsV2(builder.build()))
  }

  def deleteObject(client: S3AsyncClient, bucket: String, key: String) =
    ZIO.fromCompletableFuture(client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build())).unit

  def deleteObjects(client: S3AsyncClient, bucket: String, identifiers: List[ObjectIdentifier]) =
    ZIO.fromCompletableFuture(
      client.deleteObjects(DeleteObjectsRequest.builder().bucket(bucket)
      .delete(Delete.builder().objects(identifiers.asJava).build()).build())
    ).unit

  def getObject(client: S3AsyncClient,
                bucket: String,
                key: String,
                ifMatch: Option[String] = None,
                ifNoneMatch: Option[String] = None,
                ifModifiedSince: Option[Instant] = None,
                ifUnmodifiedSince: Option[Instant] = None,
                range: Option[String] = None) = {

    val readStream = ReactiveReadStream.readStream[Buffer]
    var response = Option.empty[GetObjectResponse]

    val builder = GetObjectRequest.builder()
      .bucket(bucket)
      .key(key)

      if (ifMatch.nonEmpty) builder.ifMatch(ifMatch.get)
      if (ifNoneMatch.nonEmpty) builder.ifNoneMatch(ifNoneMatch.get)
      if (ifModifiedSince.nonEmpty) builder.ifModifiedSince(ifModifiedSince.get)
      if (ifUnmodifiedSince.nonEmpty) builder.ifUnmodifiedSince(ifUnmodifiedSince.get)
      if (range.nonEmpty) builder.range(range.get)

    ZIO.fromCompletableFuture(client.getObject(builder.build(), new AsyncResponseTransformer[GetObjectResponse, Unit] {
      override def onStream(publisher: SdkPublisher[ByteBuffer]) =
        publisher.subscribe(new Subscriber[ByteBuffer] {
          override def onSubscribe(sub: Subscription) = readStream.onSubscribe(sub)
          override def onNext(t: ByteBuffer) = readStream.onNext(Buffer.buffer(t.array()))
          override def onError(t: Throwable) = readStream.onError(t)
          override def onComplete() = readStream.onComplete()
        })

      override def prepare() = new CompletableFuture[Unit] {}
      override def onResponse(r: GetObjectResponse) = response = Some(r)
      override def exceptionOccurred(error: Throwable) = readStream.onError(error)
    })).as((response.get, readStream))
  }

  def getObjectAttributes(client: S3AsyncClient, bucket: String, key: String) =
    ZIO.fromCompletableFuture(client.getObjectAttributes(GetObjectAttributesRequest.builder().bucket(bucket).key(key).build()))

  def getObjectTagging(client: S3AsyncClient, bucket: String, key: String) =
    ZIO.fromCompletableFuture(client.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucket).key(key).build()))

  def putObject(client: S3AsyncClient,
                bucket: String,
                key: String,
                writeStream: ReactiveWriteStream[Buffer],
                acl: ObjectCannedACL,
                contentLength: Option[Long] = None,
                contentMD5: Option[String] = None,
                storageClass: Option[String] = None,
                tags: Map[String, String] = Map()) =
    ZIO.fromCompletableFuture {
      val builder = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .acl(acl)
        .tagging(Tagging.builder().tagSet(tags.map { case (k,v) => Tag.builder().key(k).value(v).build() }.toList.asJava).build())

      if (contentLength.nonEmpty) builder.contentLength(contentLength.get)
      if (contentMD5.nonEmpty) builder.contentMD5(contentMD5.get)
      if (storageClass.nonEmpty) builder.storageClass(storageClass.get)

      client.putObject(builder.build(), publisher(writeStream))
    }.map(_.eTag())

  def copyObject(client: S3AsyncClient,
                 sourceBucket: String,
                 sourceKey: String,
                 destinationBucket: String,
                 destinationKey: String,
                 sourceIfMatch: Option[String] = None,
                 sourceIfNoneMatch: Option[String] = None,
                 sourceIfModifiedSince: Option[Instant] = None,
                 sourceIfUnmodifiedSince: Option[Instant] = None) = {

    val builder = CopyObjectRequest.builder()
    .sourceBucket(sourceBucket).sourceKey(sourceKey)
    .destinationBucket(destinationBucket).destinationKey(destinationKey)

    if (sourceIfMatch.nonEmpty) builder.copySourceIfMatch(sourceIfMatch.get)
    if (sourceIfNoneMatch.nonEmpty) builder.copySourceIfNoneMatch(sourceIfNoneMatch.get)
    if (sourceIfModifiedSince.nonEmpty) builder.copySourceIfModifiedSince(sourceIfModifiedSince.get)
    if (sourceIfUnmodifiedSince.nonEmpty) builder.copySourceIfUnmodifiedSince(sourceIfUnmodifiedSince.get)

    ZIO.fromCompletableFuture(client.copyObject(builder.build())).map(_.copyObjectResult())
  }

  def getObjectAcl(client: S3AsyncClient, bucket: String, key: String) =
    ZIO.fromCompletableFuture(client.getObjectAcl(GetObjectAclRequest.builder().bucket(bucket).key(key).build()))

  def putObjectAcl(client: S3AsyncClient, bucket: String, key: String, acl: ObjectCannedACL) =
    ZIO.fromCompletableFuture(client.putObjectAcl(PutObjectAclRequest.builder().bucket(bucket).key(key).acl(acl).build())).unit

  def putObjectTagging(client: S3AsyncClient, bucket: String, key: String, tags: Map[String, String]) =
    ZIO.fromCompletableFuture(client.putObjectTagging(
      PutObjectTaggingRequest.builder().bucket(bucket).key(key)
        .tagging(Tagging.builder().tagSet(tags.map { case (k, v) => Tag.builder().key(k).value(v).build() }.toList.asJava).build())
        .build())
    ).unit

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
                     copySourceRange: Option[String]) =
    ZIO.fromCompletableFuture {
      val builder = UploadPartCopyRequest.builder()
        .sourceBucket(sourceBucket).sourceKey(sourceKey)
        .destinationBucket(destinationBucket).destinationKey(destinationKey)
        .partNumber(partNumber).uploadId(uploadId)

      if (copySourceRange.nonEmpty) builder.copySourceRange(copySourceRange.get)
      if (copySourceIfMatch.nonEmpty) builder.copySourceIfMatch(copySourceIfMatch.get)
      if (copySourceIfNoneMatch.nonEmpty) builder.copySourceIfNoneMatch(copySourceIfNoneMatch.get)
      if (copySourceIfModifiedSince.nonEmpty) builder.copySourceIfModifiedSince(copySourceIfModifiedSince.get)
      if (copySourceIfUnmodifiedSince.nonEmpty) builder.copySourceIfUnmodifiedSince(copySourceIfUnmodifiedSince.get)

      client.uploadPartCopy(builder.build())
    }.map(_.copyPartResult())

  def uploadPart(client: S3AsyncClient,
                 bucket: String,
                 key: String,
                 uploadId: String,
                 partNumber: Int,
                 writeStream: ReactiveWriteStream[Buffer],
                 contentLength: Option[Long] = None) = {
    val builder = UploadPartRequest.builder().bucket(bucket).key(key).partNumber(partNumber).uploadId(uploadId)
    if (contentLength.nonEmpty) builder.contentLength(contentLength.get)
    ZIO.fromCompletableFuture(client.uploadPart(builder.build(), AsyncRequestBody.fromPublisher(publisher(writeStream)))).map(_.eTag())
  }

  def listParts(client: S3AsyncClient, bucket: String, key: String, uploadId: String) =
    ZIO.fromCompletableFuture(client.listParts(ListPartsRequest.builder().bucket(bucket).key(key).uploadId(uploadId).build())).map(_.parts().asScala.toList)

  def listMultipartUploads(client: S3AsyncClient, bucket: String, prefix: Option[String] = None) = {
    val builder = ListMultipartUploadsRequest.builder().bucket(bucket)
    if (prefix.nonEmpty) builder.prefix(prefix.get)
    ZIO.fromCompletableFuture(client.listMultipartUploads(builder.build())).map(_.uploads().asScala.toList)
  }

  def createMultipartUpload(client: S3AsyncClient, bucket: String, key: String, cannedACL: ObjectCannedACL) =
    ZIO.fromCompletableFuture(client.createMultipartUpload(CreateMultipartUploadRequest.builder().bucket(bucket).key(key).acl(cannedACL).build())).map(_.uploadId())

  def abortMultipartUpload(client: S3AsyncClient, bucket: String, key: String, uploadId: String) =
    ZIO.fromCompletableFuture(client.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId).build())).unit

  def completeMultipartUpload(client: S3AsyncClient, bucket: String, key: String, uploadId: String) =
    ZIO.fromCompletableFuture(client.completeMultipartUpload(CompleteMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId).build())).map(_.eTag())

  def presignedUrl(bucketName: String, key: String, expirationMinutes: Int): Task[String] = {
    ZIO.attempt {
      val presignRequest = GetObjectPresignRequest.builder.signatureDuration(Duration.ofMinutes(expirationMinutes)).build()
      presigner.presignGetObject(presignRequest).url().toString
    }
  }

  private def publisher(writeStream: ReactiveWriteStream[Buffer]) =
    new AsyncRequestBody() {
      def contentLength: Optional[java.lang.Long] = Optional.empty
      def subscribe(s: Subscriber[_ >: ByteBuffer]) =
        writeStream.subscribe(new Subscriber[Buffer] {
          def onSubscribe(sub: Subscription) = s.onSubscribe(sub)
          def onNext(t: Buffer) = s.onNext(t.getByteBuf.nioBuffer())
          def onError(t: Throwable) = s.onError(t)
          def onComplete() = s.onComplete()
        })
    }
}