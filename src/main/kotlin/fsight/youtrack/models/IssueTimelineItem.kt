package fsight.youtrack.models

import java.sql.Timestamp

data class IssueTimelineItem(
        var id: String,
        var date: Timestamp,
        var stateOld: String?,
        var stateNew: String?,
        var timeSpent: Long?,
        var stateOwner: String? = "unassigned"
)

