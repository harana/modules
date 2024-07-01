import sbt._
import com.harana.sbt.common._

val modules = haranaProject("modules").in(file("."))
  .settings(
    unmanagedBase := baseDirectory.value / "lib",
    libraryDependencies ++=
      Library.airbyte.value ++
      Library.alluxio.value ++
      Library.awsS3v2.value ++
      Library.circe.value ++
      Library.dockerJava.value ++
      Library.googleServiceApi.value ++
      Library.jackson.value ++
      Library.jgrapht.value ++
      Library.json4s.value ++
      Library.netty.value ++
      Library.pac4j.value ++
      Library.scala.value ++
      Library.testing.value ++
      Library.vertx.value ++
      Library.vfs.value ++
      Library.zio2.value :+
      Library.airtable.value :+
      Library.avro4s.value :+
      Library.awsJavaSes.value :+
      Library.awsScalaIam.value :+
      Library.awsScalaS3.value :+
      Library.auth0.value :+
      Library.betterFiles.value :+
      Library.calciteCore.value :+
      Library.chargebee.value :+
      Library.chimney.value :+
      Library.commonsEmail.value :+
      Library.deepstream.value :+
      Library.facebook.value :+
      Library.kryo.value :+
      Library.handlebars.value :+
      Library.jasyncfio.value :+
      Library.javaWebsocket.value :+
      Library.jbrowserDriver.value :+
      Library.jgit.value :+
      Library.jsch.value :+
      Library.jsoup.value :+
      Library.kubernetesClient.value :+
      Library.meilisearch.value :+
      Library.mixpanel.value :+
      Library.ognl.value :+
      Library.ohc.value :+
      Library.playJsonExtensions.value :+
      Library.pureCsv.value :+
      Library.redisson.value :+
      Library.segment.value :+
      Library.sentry.value :+
      Library.shopify.value :+
      Library.skuber.value :+
      Library.sshj.value :+
      Library.siteCrawler.value :+
      Library.snappy.value :+
      Library.slack.value :+
      Library.stripe.value :+
      Library.sundial.value :+
      Library.thumbnailator.value :+
      Library.unboundid.value :+
      Library.youiClient.value :+
      Library.zendeskClient.value :+
      Library.zip4j.value :+
      Library.zstd.value :+
      Library.ztZip.value :+
      "com.harana" %%% "modules-core" % "1.0.1" :+
      "com.harana" %%% "sdk" % "1.0.1"
  )
