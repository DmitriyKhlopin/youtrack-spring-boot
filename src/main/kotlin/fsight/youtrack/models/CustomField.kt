package fsight.youtrack.models

import fsight.youtrack.models.FieldInstance

data class CustomField(
    var name: String?,
    var id: String?,
    var aliases: String?,
    var instances: List<FieldInstance>?,
    val `$type`: String?
)
