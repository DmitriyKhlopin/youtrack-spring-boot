package fsight.youtrack.etl.bundles

data class CustomField(
    var name: String?,
    var id: String?,
    var aliases: String?,
    var instances: List<FieldInstance>?,
    val `$type`: String?
)
