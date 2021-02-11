package fsight.youtrack.models

import fsight.youtrack.generated.jooq.tables.records.IssueTimelineDetailedRecord
import fsight.youtrack.generated.jooq.tables.records.IssueTimelineRecord
import java.sql.Timestamp

data class IssueTimelineItem(
    var order: Int?,
    var id: String,
    var dateFrom: Timestamp,
    var dateTo: Timestamp,
    var stateOld: String?,
    var stateNew: String?,
    var timeSpent: Int?,
    var stateOwner: Int? = -1
)

fun IssueTimelineItem.toIssueTimelineRecord() =
    IssueTimelineRecord(
        this.order,
        this.id,
        this.stateOld ?: "undefined",
        this.stateNew ?: "undefined",
        this.dateFrom,
        this.dateTo,
        this.timeSpent,
        this.stateOwner ?: -1
    )

fun IssueTimelineItem.toIssueDetailedTimelineRecord() =
    IssueTimelineDetailedRecord(
        this.order,
        this.id,
        this.stateOld ?: "undefined",
        this.stateNew ?: "undefined",
        this.dateFrom,
        this.dateTo,
        this.timeSpent,
        this.stateOwner ?: -1
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
        this.stateOwner.toString()
    )

data class IssueTimelineItemForWeb(
    var order: Int?,
    var id: String,
    var dateFrom: String,
    var dateTo: String,
    var stateOld: String?,
    var stateNew: String?,
    var timeSpent: Int?,
    var stateOwner: String? = "unassigned"
)
