package fsight.youtrack.etl.bundles

data class FieldInstance(
    val id: String?,
    val project: InstanceProject?,
    val bundle: BundleInstance?,
    val `$type`: String?
)