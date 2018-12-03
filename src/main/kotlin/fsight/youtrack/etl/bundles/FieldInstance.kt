package fsight.youtrack.etl.bundles

data class FieldInstance(
    val id: String?,
    val project: ProjectInstance?,
    val bundle: BundleInstance?,
    val `$type`: String?
)