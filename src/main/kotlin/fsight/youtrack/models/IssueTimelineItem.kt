package fsight.youtrack.models

import fsight.youtrack.generated.jooq.tables.records.IssueTimelineRecord
import java.sql.Timestamp

data class IssueTimelineItem(
    var id: String,
    var dateFrom: Timestamp,
    var dateTo: Timestamp,
    var stateOld: String?,
    var stateNew: String?,
    var timeSpent: Long?,
    var stateOwner: String? = "unassigned"
)

fun IssueTimelineItem.toIssueTimelineRecord() =
    IssueTimelineRecord(
        this.id,
        this.stateOld,
        this.stateNew,
        this.dateFrom,
        this.dateTo,
        this.timeSpent,
        this.stateOwner
    )
