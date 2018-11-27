package fsight.youtrack.etl.bundles

data class BundleValue(
    val id: String?,
    val name: String?,
    var projectId: String? = null,
    var projectName: String? = null,
    var fieldId: String? = null,
    var fieldName: String? = null,
    var `$type`: String?
)