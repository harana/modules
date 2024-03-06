package com.harana.modules.aws

import awscala._
import awscala.iam.{AccessKey, IAM}
import awscala.s3.{Bucket, S3, S3ObjectSummary}
import com.amazonaws.auth._
import com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsync, AmazonSimpleEmailServiceAsyncClient}
import com.harana.modules.aws.LiveAWS._
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import io.circe.syntax._
import zio.{Task, ZIO, ZLayer}

import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

object LiveAWS {
  val credentialsProviderRef = new AtomicReference[Option[AWSCredentialsProvider]](None)
  val iamRef = new AtomicReference[Option[IAM]](None)
  val s3Ref = new AtomicReference[Option[S3]](None)
  val sesRef = new AtomicReference[Option[AmazonSimpleEmailServiceAsync]](None)

  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAWS(config, logger, micrometer)
  }
}

case class LiveAWS(config: Config, logger: Logger, micrometer: Micrometer) extends AWS {

  private def credentialsProvider =
    for {
      provider <- if (credentialsProviderRef.get.nonEmpty) ZIO.attempt(credentialsProviderRef.get.get) else
        for {
          accessId                  <- config.secret("aws-access-id")
          secretKey                 <- config.secret("aws-secret-key")
          credentialsFile           <- config.optString("aws.credentialsFile")
          useCredentialsFile        <- config.boolean("aws.useCredentialsFile", default = false)
          useEnvironmentVariables   <- config.boolean("aws.useEnvironmentVariables", default = false)
          useInstanceProfile        <- config.boolean("aws.useInstanceProfile", default = false)
          profile                   <- config.optString("aws.profile")
          provider                  = (accessId, secretKey, credentialsFile, useCredentialsFile, useEnvironmentVariables, useInstanceProfile, profile) match {
                                        case (_, _, _, _, true, _, _) => new EnvironmentVariableCredentialsProvider()
                                        case (_, _, _, _, _, true, _) => InstanceProfileCredentialsProvider.getInstance()
                                        case (a, s, _, _, _, _, _) => new AWSStaticCredentialsProvider(new BasicAWSCredentials(a, s))
                                      }
        } yield provider
      _ = credentialsProviderRef.set(Some(provider))
    } yield provider


  private def iamClient =
    for {
      client <- if (iamRef.get.nonEmpty) ZIO.attempt(iamRef.get.get) else
                  for {
                    creds   <- credentialsProvider
                    iam     <- ZIO.attempt(IAM(creds))
                  } yield iam
                _ = iamRef.set(Some(client))
    } yield client


  private def s3Client =
    for {
      client <- if (s3Ref.get.nonEmpty) ZIO.attempt(s3Ref.get.get) else
                  for {
                    creds   <- credentialsProvider
                    region  <- config.secret("aws-region")
                    s3      <- ZIO.attempt(S3(creds)(awscala.Region(region)))
                  } yield s3
           _ = s3Ref.set(Some(client))
    } yield client


  private def sesClient =
    for {
      client <- if (sesRef.get.nonEmpty) ZIO.attempt(sesRef.get.get) else
        for {
          creds     <- credentialsProvider
          region    <- config.secret("aws-region")
          client    <- ZIO.succeed(AmazonSimpleEmailServiceAsyncClient.asyncBuilder().withCredentials(creds).withRegion(region).build())
        } yield client
      _ = sesRef.set(Some(client))
    } yield client


  private def s3Bucket(bucket: String): Task[Bucket] =
    for {
      client        <- s3Client
      bucket        <- ZIO.fromOption(client.bucket(bucket)).orElseFail(new Throwable("No available bucket"))
    } yield bucket


  def iamCreateS3User(name: String, bucket: String, prefix: String): Task[AccessKey] =
    for {
      client        <- iamClient
      user          <- ZIO.attempt(client.createUser(name))
      s3arn         =  s"arn:aws:s3:::$bucket/$prefix"
      policy        =  Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource(s3arn)))))
      _             =  user.putPolicy(s"$name-s3", policy)(client)
      accessKey     =  user.createAccessKey()(client)
    } yield accessKey


  def iamDeleteUser(name: String): Task[Unit] =
    for {
      client        <- iamClient
      user          <- ZIO.attempt(client.user(name))
      _             <- ZIO.attempt(client.deleteUserPolicy(new DeleteUserPolicyRequest().withUserName(name).withPolicyName(s"$name-s3")))
      _             <- ZIO.foreachDiscard(user)(u => ZIO.attempt(client.delete(u)))
    } yield ()


  def s3CreateBucket(name: String): Task[Unit] =
    for {
      client        <- s3Client
      _             <- ZIO.attempt(client.createBucket(name))
    } yield ()


  def s3List(bucket: String, prefix: Option[String]): Task[List[S3ObjectSummary]] =
    for {
      client        <- s3Client
      bucket        <- s3Bucket(bucket)
      summaries     <- ZIO.attempt(client.objectSummaries(bucket, prefix.getOrElse("")).toList)
    } yield summaries


  def s3ListAsStream(bucket: String, prefix: Option[String]): Task[Stream[Either[String, S3ObjectSummary]]] =
    for {
      client        <- s3Client
      bucket        <- s3Bucket(bucket)
      summaries     <- ZIO.attempt(client.ls(bucket, prefix.getOrElse("")))
    } yield summaries


  def s3ListTags(bucket: String, at: String): Task[Map[String, String]] =
    for {
      client        <- s3Client
      request       =  new GetObjectTaggingRequest(bucket, at)
      tagging       <- ZIO.attempt(client.getObjectTagging(request))
      tags          =  tagging.getTagSet.asScala.map(t => t.getKey -> t.getValue).toMap
    } yield tags


  def s3CopyFile(fromBucket: String, from: String, toBucket: Option[String], to: String): Task[Unit] =
    for {
      client        <- s3Client
      _             <- ZIO.attempt(client.copyObject(fromBucket, from, toBucket.getOrElse(fromBucket), to))
    } yield ()


  def s3CopyFolder(fromBucket: String, from: String, toBucket: Option[String], to: String): Task[Unit] =
    for {
      client        <- s3Client
      manager       <- ZIO.attempt {
                          val tm = TransferManagerBuilder.standard
                          tm.setS3Client(client)
                          tm.build
                        }
      fromFiles     <- ZIO.attempt(client.listObjects(fromBucket, s"$from/").getObjectSummaries.asScala.toList)
      _             <- ZIO.foreachParDiscard(fromFiles) { file =>
                          for {
                            filename <- ZIO.succeed(file.getKey.replace(s"$from/", ""))
                            _ <- logger.debug(s"Copying $fromBucket/${file.getKey} to $toBucket/$to/$filename")
                            _ <- ZIO.attempt(manager.copy(fromBucket, file.getKey, toBucket.getOrElse(fromBucket), s"$to/$filename").waitForCompletion()).when(!filename.isEmpty)
                          } yield ()
                        }
    } yield ()


  def s3Move(fromBucket: String, from: String, toBucket: Option[String], to: String): Task[Unit] =
    for {
      _             <- s3CopyFile(fromBucket, from, toBucket, to)
      _             <- s3Delete(fromBucket, from)
    } yield ()


  def s3Rename(bucket: String, from: String, to: String): Task[Unit] =
    s3Move(bucket, from, None, to)


  def s3Get(bucket: String, at: String): Task[InputStream] =
    for {
      client        <- s3Client
      bucket        <- s3Bucket(bucket)
      inputStream   <- ZIO.attempt(client.getObject(bucket, at).get.content)
    } yield inputStream


  def s3Put(bucket: String, at: String, inputStream: InputStream, contentLength: Long): Task[Unit] =
    for {
      client        <- s3Client
      metadata      =  new ObjectMetadata()
      _             =  metadata.setContentLength(contentLength)
      _             <- ZIO.attempt(client.putObject(bucket, at, inputStream, metadata))
    } yield ()


  def s3Delete(bucket: String, at: String): Task[Unit] =
    for {
      client        <- s3Client
      _             <- ZIO.attempt(client.deleteObject(bucket, at))
    } yield ()


  def s3Tag(bucket: String, at: String, tags: Map[String, String]): Task[Unit] =
    for {
      client        <- s3Client
      s3Tags        =  tags.map { case (k, v) => new Tag(k, v) }
      request       =  new SetObjectTaggingRequest(bucket, at, new ObjectTagging(s3Tags.toList.asJava))
      _             <- ZIO.attempt(client.setObjectTagging(request))
    } yield ()


  def sesCreateTemplate(template: Template): Task[Unit] =
    for {
      client              <- sesClient
      request             =  new CreateTemplateRequest().withTemplate(template)
      -                   <- ZIO.fromFutureJava(client.createTemplateAsync(request))
    } yield ()


  def sesDeleteTemplate(name: String): Task[Unit] =
    for {
      client              <- sesClient
      request             =  new DeleteTemplateRequest().withTemplateName(name)
      -                   <- ZIO.fromFutureJava(client.deleteTemplateAsync(request))
    } yield ()


  def sesSendEmail(message: Message, to: List[String], cc: List[String], bcc: List[String], sender: String, replyTo: List[String] = List()): Task[Unit] =
    for {
      client              <- sesClient
      destination         =  new Destination().withBccAddresses(bcc.asJava).withCcAddresses(cc.asJava).withToAddresses(to.asJava)
      configurationSet    <- config.string("aws.ses.configurationSet", "default")
      request             =  new SendEmailRequest()
                              .withConfigurationSetName(configurationSet)
                              .withSource(sender)
                              .withDestination(destination)
                              .withMessage(message)
                              .withReplyToAddresses(replyTo.asJava)
      -                   <- ZIO.fromFutureJava(client.sendEmailAsync(request))
    } yield ()


  def sesSendTemplatedEmail(template: String, templateValues: Map[String, String], to: List[String], cc: List[String], bcc: List[String], sender: String, replyTo: List[String] = List()): Task[Unit] =
    for {
      client              <- sesClient
      destination         =  new Destination().withBccAddresses(bcc.asJava).withCcAddresses(cc.asJava).withToAddresses(to.asJava)
      configurationSet    <- config.string("aws.ses.configurationSet", "default")
      request             =  new SendTemplatedEmailRequest()
                              .withConfigurationSetName(configurationSet)
                              .withSource(sender)
                              .withDestination(destination)
                              .withReplyToAddresses(replyTo.asJava)
                              .withTemplate(template)
                              .withTemplateData(templateValues.asJson.noSpaces)
      -                   <- ZIO.fromFutureJava(client.sendTemplatedEmailAsync(request))
    } yield ()


  def sesSendBulkTemplatedEmail(template: String, toWithTemplateValues: List[(String, Map[String, String])], sender: String, replyTo: List[String] = List()): Task[Unit] =
    for {
      client              <- sesClient
      configurationSet    <- config.string("aws.ses.configurationSet", "default")
      destinations        =  toWithTemplateValues.map(t =>
                                new BulkEmailDestination()
                                  .withDestination(new Destination().withToAddresses(List(t._1).asJava))
                                  .withReplacementTemplateData(t._2.asJson.noSpaces)
                             ).asJavaCollection
      request             =  new SendBulkTemplatedEmailRequest()
                              .withConfigurationSetName(configurationSet)
                              .withSource(sender)
                              .withDestinations(destinations)
                              .withDefaultTemplateData("{}")
                              .withTemplate(template)
                              .withReplyToAddresses(replyTo.asJava)
      -                   <- ZIO.fromFutureJava(client.sendBulkTemplatedEmailAsync(request))
    } yield ()
}