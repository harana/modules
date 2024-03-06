package com.harana.modules.plugin

import com.harana.models.{PluginInfo, PluginServiceInfo}
import com.harana.modules.plugin.models.PluginError
import com.harana.sdk.shared.plugin.Service
import com.harana.sdk.shared.plugin.Service.ServiceId
import zio.IO
import zio.macros.accessible

import java.net.URL
import scala.reflect.ClassTag

@accessible
trait Plugin {
    def findPlugins[T <: Service](implicit cmf: ClassTag[T]): IO[PluginError, List[PluginInfo]]

    def findServices[T <: Service](implicit cmf: ClassTag[T]): IO[PluginError, Map[ServiceId, T]]

    def findServiceInfos[T <: Service](implicit cmf: ClassTag[T]): IO[PluginError, List[PluginServiceInfo]]

    def getService[T <: Service](serviceId: ServiceId)(implicit cmf: ClassTag[T]): IO[PluginError, T]

    def getResource(className: String, fileName: String): IO[PluginError, URL]
}