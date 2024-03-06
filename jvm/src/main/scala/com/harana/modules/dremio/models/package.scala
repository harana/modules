package com.harana.modules.dremio

import enumeratum.values._
import io.circe.generic.JsonCodec

package object models {

  type EntityId = String
  type JobId = String

  sealed abstract class AWSElasticsearchAuthType(val value: String) extends StringEnumEntry
  case object AWSElasticsearchAuthType extends StringEnum[AWSElasticsearchAuthType] with StringCirceEnum[AWSElasticsearchAuthType] {
    case object AccessKey extends AWSElasticsearchAuthType("ACCESS_KEY")
    case object EC2Metadata extends AWSElasticsearchAuthType("EC2_METADATA")
    case object None extends AWSElasticsearchAuthType("NONE")
    val values = findValues
  }

  sealed abstract class AWSElasticsearchEncryptionValidationMode(val value: String) extends StringEnumEntry
  case object AWSElasticsearchEncryptionValidationMode extends StringEnum[AWSElasticsearchEncryptionValidationMode] with StringCirceEnum[AWSElasticsearchEncryptionValidationMode] {
    case object CertificateAndHostnameValidation extends AWSElasticsearchEncryptionValidationMode("CERTIFICATE_AND_HOSTNAME_VALIDATION")
    case object CertificateOnlyValidation extends AWSElasticsearchEncryptionValidationMode("CERTIFICATE_ONLY_VALIDATION")
    case object NoValidation extends AWSElasticsearchEncryptionValidationMode("NO_VALIDATION")
    val values = findValues
  }

  sealed abstract class StandardAuthType(val value: String) extends StringEnumEntry
  case object StandardAuthType extends StringEnum[StandardAuthType] with StringCirceEnum[StandardAuthType] {
    case object Anonymous extends StandardAuthType("ANONYMOUS")
    case object Master extends StandardAuthType("MASTER")
    val values = findValues
  }

  sealed abstract class DatasetUpdateMode(val value: String) extends StringEnumEntry
  case object DatasetUpdateMode extends StringEnum[DatasetUpdateMode] with StringCirceEnum[DatasetUpdateMode] {
    case object Anonymous extends DatasetUpdateMode("PREFETCH")
    case object Master extends DatasetUpdateMode("PREFETCH_QUERIED")
    case object Inline extends DatasetUpdateMode("INLINE")
    val values = findValues
  }

  sealed abstract class ContainerType(val value: String) extends StringEnumEntry
  case object ContainerType extends StringEnum[ContainerType] with StringCirceEnum[ContainerType] {
    case object Home extends ContainerType("HOME")
    case object Folder extends ContainerType("FOLDER")
    case object Source extends ContainerType("SOURCE")
    case object Space extends ContainerType("SPACE")
    val values = findValues
  }

  sealed abstract class DatasetType(val value: String) extends StringEnumEntry
  case object DatasetType extends StringEnum[DatasetType] with StringCirceEnum[DatasetType] {
    case object Physical extends DatasetType("PHYSICAL_DATASET")
    case object Virtual extends DatasetType("VIRTUAL_DATASET")
    val values = findValues
  }

  sealed abstract class EntitySummaryType(val value: String) extends StringEnumEntry
  case object EntitySummaryType extends StringEnum[EntitySummaryType] with StringCirceEnum[EntitySummaryType] {
    case object Dataset extends EntitySummaryType("DATASET")
    case object Container extends EntitySummaryType("CONTAINER")
    case object File extends EntitySummaryType("FILE")
    val values = findValues
  }

  sealed abstract class EntitySummaryDatasetType(val value: String) extends StringEnumEntry
  case object EntitySummaryDatasetType extends StringEnum[EntitySummaryDatasetType] with StringCirceEnum[EntitySummaryDatasetType] {
    case object Virtual extends EntitySummaryDatasetType("VIRTUAL")
    case object Promoted extends EntitySummaryDatasetType("PROMOTED")
    case object Direct extends EntitySummaryDatasetType("DIRECT")
    val values = findValues
  }

  sealed abstract class AccelerationRefreshPolicyMethod(val value: String) extends StringEnumEntry
  case object AccelerationRefreshPolicyMethod extends StringEnum[AccelerationRefreshPolicyMethod] with StringCirceEnum[AccelerationRefreshPolicyMethod] {
    case object Full extends AccelerationRefreshPolicyMethod("FULL")
    case object Incremental extends AccelerationRefreshPolicyMethod("INCREMENTAL")
    val values = findValues
  }

  sealed abstract class JobStateJobQueryType(val value: String) extends StringEnumEntry
  case object JobStateJobQueryType extends StringEnum[JobStateJobQueryType] with StringCirceEnum[JobStateJobQueryType] {
    case object Pending extends JobStateJobQueryType("PENDING")
    case object MetadataRetrieval extends JobStateJobQueryType("METADATA_RETRIEVAL")
    case object Planning extends JobStateJobQueryType("PLANNING")
    case object Queued extends JobStateJobQueryType("QUEUED")
    case object EngineStart extends JobStateJobQueryType("ENGINE_START")
    case object ExecutionPlanning extends JobStateJobQueryType("EXECUTION_PLANNING")
    case object Starting extends JobStateJobQueryType("STARTING")
    case object Running extends JobStateJobQueryType("RUNNING")
    case object Completed extends JobStateJobQueryType("COMPLETED")
    case object Cancelled extends JobStateJobQueryType("CANCELED")
    case object Failed extends JobStateJobQueryType("FAILED")
    val values = findValues
  }

  sealed abstract class JobQueryType(val value: String) extends StringEnumEntry
  case object JobQueryType extends StringEnum[JobQueryType] with StringCirceEnum[JobQueryType] {
    case object UIRun extends JobQueryType("UI_RUN")
    case object UIPreview extends JobQueryType("UI_PREVIEW")
    case object UIInternalPreview extends JobQueryType("UI_INTERNAL_PREVIEW")
    case object UIInternalRun extends JobQueryType("UI_INTERNAL_RUN")
    case object UIExport extends JobQueryType("UI_EXPORT")
    case object ODBC extends JobQueryType("ODBC")
    case object JDBC extends JobQueryType("JDBC")
    case object REST extends JobQueryType("REST")
    case object AcceleratorCreate extends JobQueryType("ACCELERATOR_CREATE")
    case object AcceleratorDrop extends JobQueryType("ACCELERATOR_DROP")
    case object Unknown extends JobQueryType("UNKNOWN")
    case object PrepareInernal extends JobQueryType("PREPARE_INTERNAL")
    case object AcceleratorExplain extends JobQueryType("ACCELERATOR_EXPLAIN")
    case object UIInitialPreview extends JobQueryType("UI_INITIAL_PREVIEW")
    val values = findValues
  }

  sealed abstract class JobAccelerationRelationshipType(val value: String) extends StringEnumEntry
  case object JobAccelerationRelationshipType extends StringEnum[JobAccelerationRelationshipType] with StringCirceEnum[JobAccelerationRelationshipType] {
    case object Considered extends JobAccelerationRelationshipType("CONSIDERED")
    case object Matched extends JobAccelerationRelationshipType("MATCHED")
    case object Chosen extends JobAccelerationRelationshipType("CHOSEN")
    val values = findValues
  }

  sealed abstract class DatasetFieldName(val value: String) extends StringEnumEntry
  case object DatasetFieldName extends StringEnum[DatasetFieldName] with StringCirceEnum[DatasetFieldName] {
    case object Struct extends DatasetFieldName("STRUCT")
    case object List extends DatasetFieldName("LIST")
    case object Union extends DatasetFieldName("UNION")
    case object Integer extends DatasetFieldName("INTEGER")
    case object Bigint extends DatasetFieldName("BIGINT")
    case object Float extends DatasetFieldName("FLOAT")
    case object Double extends DatasetFieldName("DOUBLE")
    case object Varchar extends DatasetFieldName("VARCHAR")
    case object Varbinary extends DatasetFieldName("VARBINARY")
    case object Boolean extends DatasetFieldName("BOOLEAN")
    case object Decimal extends DatasetFieldName("DECIMAL")
    case object Time extends DatasetFieldName("TIME")
    case object Date extends DatasetFieldName("DATE")
    case object Timestamp extends DatasetFieldName("TIMESTAMP")
    case object IntervalDayToSecond extends DatasetFieldName("INTERVAL DAY TO SECOND")
    case object IntervalDayToMonth extends DatasetFieldName("INTERVAL YEAR TO MONTH")
    val values = findValues
  }

  @JsonCodec
  case class Host(hostname: String, port: Int)

  @JsonCodec
  case class Property(name: String, value: String)
}