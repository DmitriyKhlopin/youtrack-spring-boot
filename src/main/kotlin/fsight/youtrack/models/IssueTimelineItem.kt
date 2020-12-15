package fsight.youtrack.models

import fsight.youtrack.generated.jooq.tables.records.IssueDetailedTimelineRecord
import fsight.youtrack.generated.jooq.tables.records.IssueTimelineRecord
import java.sql.Timestamp

data class IssueTimelineItem(
    var order: Int?,
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

fun IssueTimelineItem.toIssueDetailedTimelineRecord() =
    IssueDetailedTimelineRecord(
        this.order,
        this.id,
        this.dateFrom,
        this.dateTo,
        this.stateOld ?: "undefined",
        this.stateNew ?: "undefined",
        this.timeSpent?.toInt() ?: 0,
        this.stateOwner?.toInt() ?: -1
    )

fun IssueTimelineItem.toReadableItem() =
    IssueTimelineItemForWeb(
        this.order,
        this.id,
        this.dateFrom.toLocalDateTime().toString(),
        this.dateTo.toLocalDateTime().toString(),
        this.stateOld,
        this.stateNew,
        this.timeSpent,
        this.stateOwner
    )

data class IssueTimelineItemForWeb(
    var order: Int?,
    var id: String,
    var dateFrom: String,
    var dateTo: String,
    var stateOld: String?,
    var stateNew: String?,
    var timeSpent: Long?,
    var stateOwner: String? = "unassigned"
)
