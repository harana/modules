package com.harana.modules.kind.models

case class Cluster(nodes: List[Node],
                   apiServerListenAddress: Option[String] = None,
                   apiServerListenPort: Option[String] = None,
                   disableDefaultCNI: Option[Boolean] = None,
                   ipFamily: Option[String] = None,
                   podSubnet: Option[String] = None,
                   serviceSubnet: Option[String] = None)

case class Node(role: Option[String] = None,
                image: Option[String] = None,
                extraMounts: List[Mount] = List(),
                extraPortMappings: List[PortMapping] = List())

case class Mount(hostPath: String,
                 containerPath: String)

case class PortMapping(containerPort: Int,
                      hostPort: Int,
                      listenAddress: Option[String] = None,
                      protocol: Option[String] = None)