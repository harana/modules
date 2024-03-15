package com.harana.modules

import io.airbyte.api.client.model.generated._
import io.airbyte.commons.enums.Enums
import io.airbyte.commons.text.Names
import io.airbyte.protocol.models.{ConfiguredAirbyteCatalog, ConfiguredAirbyteStream, AirbyteCatalog => ProtocolAirbyteCatalog, AirbyteStream => ProtocolAirbyteStream, DestinationSyncMode => ProtocolDestinationSyncMode, SyncMode => ProtocolSyncMode}
import io.circe.{ACursor, HCursor, Json}
import org.apache.commons.io.IOUtils

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile
import scala.jdk.CollectionConverters._

package object airbyte {

  def toAirbyteIntegration(fileName: String, json: Json): AirbyteIntegration = {
    val connectionSpec = json.hcursor.downField("connectionSpecification")
    val required = connectionSpec.downField("required").as[List[String]].getOrElse(List())

    val properties =
      connectionSpec.downField("properties").keys.getOrElse(List()).map { p =>
        toAirbyteProperty(p, connectionSpec.downField("properties").downField(p), required)
      }

    val name = fileName.substring(fileName.indexOf("-") + 1, fileName.length)
    val syncDirection = if (fileName.contains("source")) AirbyteSyncDirection.Source else AirbyteSyncDirection.Destination
    val supportsDBT = json.hcursor.downField("supportsDBT").as[Boolean].getOrElse(false)
    val supportsIncremental = json.hcursor.downField("supportsIncremental").as[Boolean].getOrElse(false)
    val supportsNormalization = json.hcursor.downField("supportsNormalization").as[Boolean].getOrElse(false)

    AirbyteIntegration(name, properties.toList, syncDirection, supportsDBT, supportsIncremental, supportsNormalization, List())
  }

  def toAirbyteProperty(name: String, cursor: ACursor, required: List[String]): AirbyteProperty = {
    val `type` = AirbytePropertyType.withValue(cursor.downField("type").as[String].getOrElse(""))

    AirbyteProperty(
      name = name,
      `type` = `type`,
      title = cursor.downField("title").as[String].toOption,
      description = cursor.downField("description").as[String].toOption,
      placeholder = cursor.downField("examples").as[List[String]].getOrElse(List()).headOption,
      required = required.contains(name),
      validationPattern = cursor.downField("pattern").as[String].toOption,
      multiline = cursor.downField("multiline").as[Boolean].toOption.getOrElse(false),
      minimum = cursor.downField("minimum").as[Int].toOption,
      maximum = cursor.downField("maximum").as[Int].toOption,
      order = cursor.downField("order").as[Int].toOption,
      secret = cursor.downField("airbyte_secret").as[Boolean].toOption.getOrElse(false),
      options = `type` match {
        case AirbytePropertyType.Integer =>
          cursor.downField("enum").as[List[Int]].getOrElse(List()).map(AirbyteOption.Integer)

        case AirbytePropertyType.Object =>
          cursor.downField("oneOf").downArray.values.getOrElse(List()).map { o =>
            toAirbyteOption(o.hcursor)
          }.toList

        case AirbytePropertyType.String =>
          cursor.downField("enum").as[List[String]].getOrElse(List()).map(AirbyteOption.String)

        case AirbytePropertyType.Array =>
          List()

        case _ => List()
      }
    )
  }

  def toAirbyteOption(cursor: HCursor): AirbyteOption.Object = {
    val title = cursor.downField("title").as[String].toOption.get
    val description = cursor.downField("description").as[String].toOption
    val required = cursor.downField("required").as[List[String]].getOrElse(List())

    val properties = cursor.downField("properties").values.getOrElse(List()).map { p =>
      toAirbyteProperty("", p.hcursor, required)
    }

    AirbyteOption.Object(title, description, properties.toList)
  }

  implicit def toApi(stream: ProtocolAirbyteStream): AirbyteStream =
    new AirbyteStream()
      .name(stream.getName)
      .jsonSchema(stream.getJsonSchema)
      .supportedSyncModes(Enums.convertListTo(stream.getSupportedSyncModes, classOf[SyncMode]))
      .sourceDefinedCursor(stream.getSourceDefinedCursor)
      .defaultCursorField(stream.getDefaultCursorField)
      .sourceDefinedPrimaryKey(stream.getSourceDefinedPrimaryKey)
      .namespace(stream.getNamespace)


  implicit def toProtocol(stream: AirbyteStream): ProtocolAirbyteStream =
    new ProtocolAirbyteStream()
      .withName(stream.getName)
      .withJsonSchema(stream.getJsonSchema)
      .withSupportedSyncModes(Enums.convertListTo(stream.getSupportedSyncModes, classOf[ProtocolSyncMode]))
      .withSourceDefinedCursor(stream.getSourceDefinedCursor)
      .withDefaultCursorField(stream.getDefaultCursorField)
      .withSourceDefinedPrimaryKey(stream.getSourceDefinedPrimaryKey)
      .withNamespace(stream.getNamespace)


  implicit def toApi(catalog: ProtocolAirbyteCatalog): AirbyteCatalog =
    new AirbyteCatalog()
      .streams(catalog.getStreams.asScala.map(toApi)
        .map(s => new AirbyteStreamAndConfiguration().stream(s).config(generateDefaultConfiguration(s))).asJava)


  implicit def toApi(catalog: ConfiguredAirbyteCatalog): AirbyteCatalog =
    new AirbyteCatalog().streams(catalog.getStreams.asScala.map(configuredStream =>
      new AirbyteStreamAndConfiguration()
        .stream(toApi(configuredStream.getStream))
        .config(new AirbyteStreamConfiguration()
          .syncMode(Enums.convertTo(configuredStream.getSyncMode, classOf[SyncMode]))
          .cursorField(configuredStream.getCursorField)
          .destinationSyncMode(Enums.convertTo(configuredStream.getDestinationSyncMode, classOf[DestinationSyncMode]))
          .primaryKey(configuredStream.getPrimaryKey)
          .aliasName(Names.toAlphanumericAndUnderscore(configuredStream.getStream.getName))
          .selected(true)
        )
    ).asJava)


  implicit def toProtocol(catalog: AirbyteCatalog): ConfiguredAirbyteCatalog =
    new ConfiguredAirbyteCatalog().withStreams(catalog
      .getStreams
      .asScala
      .filter(_.getConfig.getSelected)
      .map(s => new ConfiguredAirbyteStream()
        .withStream(toProtocol(s.getStream))
        .withSyncMode(Enums.convertTo(s.getConfig.getSyncMode, classOf[ProtocolSyncMode]))
        .withCursorField(s.getConfig.getCursorField)
        .withDestinationSyncMode(Enums.convertTo(s.getConfig.getDestinationSyncMode, classOf[ProtocolDestinationSyncMode]))
        .withPrimaryKey(s.getConfig.getPrimaryKey)
      )
      .asJava)


  private def generateDefaultConfiguration(stream: AirbyteStream) = {
    val result = new AirbyteStreamConfiguration()
      .aliasName(Names.toAlphanumericAndUnderscore(stream.getName))
      .cursorField(stream.getDefaultCursorField)
      .destinationSyncMode(DestinationSyncMode.APPEND)
      .primaryKey(stream.getSourceDefinedPrimaryKey).selected(true)

    if (stream.getSupportedSyncModes.size > 0) result.setSyncMode(stream.getSupportedSyncModes.get(0))
    else result.setSyncMode(SyncMode.INCREMENTAL)

    result
  }

  val jarFile = new File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)

  def airbyteFiles: Map[String, String] = {
    val files = if (jarFile.isFile) {
      val jar = new JarFile(jarFile)
      val jarFiles = jar.entries.asScala.filter(_.getName.startsWith("airbyte/")).map(f => s"/${f.getName}")
      jarFiles.toList
    } else {
      val url = classOf[Airbyte].getResource("/airbyte")
      new File(url.toURI).listFiles().map(_.getName).toList
    }
    files.map(f =>
      f.replace("/", "").replace("airbyte", "").replace(".json", "") ->
        IOUtils.toString(classOf[Airbyte].getResourceAsStream(f), StandardCharsets.UTF_8.name)
    ).toMap
  }
}