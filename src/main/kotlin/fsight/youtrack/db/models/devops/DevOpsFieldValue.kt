package fsight.youtrack.db.models.devops

import java.sql.Timestamp

data class DevOpsFieldValue(
    val id: Int?,
    val fieldId: Int?,
    val int: Int?,
    val float: Float?,
    val dateTime: Timestamp?,
    val string: String?
)
