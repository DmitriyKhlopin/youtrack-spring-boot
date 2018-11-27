package fsight.youtrack.etl.bundles

data class CustomField(
    val name: String?,
    val id: String?,
    val aliases: String?,
    val instances: List<FieldInstance>?,
    val `$type`: String?
)