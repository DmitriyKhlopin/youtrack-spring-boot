package fsight.youtrack.models.sql

import java.sql.Timestamp

//TODO удалить
data class IssueHistoryItem(
    var issueId: String?,
    var author: String?,
    var updateDateTime: Timestamp?,
    var fieldName: String?,
    var value: String?,
    var oldValue: String?,
    var newValue: String?,
    var fieldType: String? = null
)
