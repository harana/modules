package com.harana.modules.aws

import awscala.iam.AccessKey
import awscala.s3.S3ObjectSummary
import com.amazonaws.services.simpleemail.model.{Message, Template}
import zio.Task
import zio.macros.accessible

import java.io.InputStream

@accessible
trait AWS {

    def iamCreateS3User(name: String, bucket: String, prefix: String): Task[AccessKey]
    def iamDeleteUser(name: String): Task[Unit]

    def s3CreateBucket(name: String): Task[Unit]
    def s3List(bucket: String, prefix: Option[String]): Task[List[S3ObjectSummary]]
    def s3ListAsStream(bucket: String, prefix: Option[String]): Task[Stream[Either[String, S3ObjectSummary]]]
    def s3ListTags(bucket: String, at: String): Task[Map[String, String]]
    def s3CopyFile(fromBucket: String, from: String, toBucket: Option[String], to: String): Task[Unit]
    def s3CopyFolder(fromBucket: String, from: String, toBucket: Option[String], to: String): Task[Unit]
    def s3Move(fromBucket: String, from: String, toBucket: Option[String], to: String): Task[Unit]
    def s3Get(bucket: String, at: String): Task[InputStream]
    def s3Put(bucket: String, at: String, inputStream: InputStream, contentLength: Long): Task[Unit]
    def s3Rename(bucket: String, from: String, to: String): Task[Unit]
    def s3Delete(bucket: String, at: String): Task[Unit]
    def s3Tag(bucket: String, at: String, tags: Map[String, String]): Task[Unit]

    def sesCreateTemplate(template: Template): Task[Unit]
    def sesDeleteTemplate(name: String): Task[Unit]
    def sesSendEmail(message: Message, to: List[String], cc: List[String], bcc: List[String], sender: String, replyTo: List[String] = List()): Task[Unit]
    def sesSendTemplatedEmail(template: String, templateValues: Map[String, String], to: List[String], cc: List[String], bcc: List[String], sender: String, replyTo: List[String] = List()): Task[Unit]
    def sesSendBulkTemplatedEmail(template: String, toWithTemplateValues: List[(String, Map[String, String])], sender: String, replyTo: List[String] = List()): Task[Unit]

}