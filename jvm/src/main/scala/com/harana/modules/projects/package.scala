package com.harana.modules

import com.github.dockerjava.api.model.AuthConfig
import com.harana.modules.kubernetes.Kubernetes
import play.api.libs.json.Format
import skuber.api.client.{KubernetesClient, LoggingContext}
import skuber.{ObjectResource, ResourceDefinition}
import zio.{Task, UIO, ZIO}

package object projects {

  def dockerAuthConfig(username: Option[String], password: Option[String], identityToken: Option[String], registryToken: Option[String]): Option[AuthConfig] =
    (username, password, identityToken, registryToken) match {
      case (Some(u), Some(p), _, _ )  => Some(new AuthConfig().withUsername(u).withPassword(p))
      case (_, _, Some(it), _ )       => Some(new AuthConfig().withIdentityToken(it))
      case (_, _, _, Some(rt) )       => Some(new AuthConfig().withRegistrytoken(rt))
      case (_, _, _, _)               => None
    }


  def customResourcesDeployed[A <: ObjectResource](client: KubernetesClient,
                                                   namespace: String,
                                                   customResources: UIO[List[A]])(kubernetes: Kubernetes)
                                                  (implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext): Task[Boolean] =
    for {
      cr          <- customResources
      deployed    <- ZIO.foreach(cr) { cr =>
                        kubernetes.get[A](client, namespace, cr.name).map(o => cr.metadata.resourceVersion.equals(o.get.metadata.resourceVersion)).either
                     }
    } yield deployed.exists(_.isLeft)



  def deployCustomResources[A <: ObjectResource](client: KubernetesClient,
                                                 namespace: String,
                                                 customResources: UIO[List[A]])(kubernetes: Kubernetes)
                                                (implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext): Task[Boolean] =
    for {
      cr          <- customResources
      deployed    <- ZIO.foreach(cr)(cr => kubernetes.create[A](client, namespace, cr).option)
    } yield deployed.exists(_.nonEmpty)



  def undeployCustomResources[A <: ObjectResource](client: KubernetesClient,
                                                   namespace: String,
                                                   customResources: UIO[List[A]])(kubernetes: Kubernetes)
                                                  (implicit fmt: Format[A], rd: ResourceDefinition[A], lc: LoggingContext): Task[Boolean] =
    for {
      cr          <- customResources
      deployed    <- ZIO.foreach(cr)(cr => kubernetes.delete[A](client, namespace, cr.name).option)
    } yield deployed.exists(_.nonEmpty)
}