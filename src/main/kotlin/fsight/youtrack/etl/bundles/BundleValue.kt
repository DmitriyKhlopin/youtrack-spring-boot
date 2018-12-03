package fsight.youtrack.etl.bundles

import fsight.youtrack.generated.jooq.tables.records.BundleValuesRecord

data class BundleValue(
    val id: String?,
    val name: String?,
    var projectId: String? = null,
    var projectName: String? = null,
    var fieldId: String? = null,
    var fieldName: String? = null,
    var `$type`: String?
)