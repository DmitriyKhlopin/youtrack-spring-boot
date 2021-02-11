package fsight.youtrack.etl.timeline

import fsight.youtrack.models.IssueTimelineItem

interface ITimeline {
    fun launchCalculation()
    fun launchCalculationForPeriod(dateFrom: String?, dateTo: String?)
    fun launchDetailedCalculationForPeriod(dateFrom: String?, dateTo: String?)
    fun calculateStateByIssueId(issueId: String)
    fun calculateDetailedStateByIssueId(issueId: String)
    fun calculatePeriod(item: IssueTimelineItem): Int
}
