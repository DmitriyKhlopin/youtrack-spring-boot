package fsight.youtrack.etl.timeline

import fsight.youtrack.models.IssueTimelineItem

interface ITimeline {
    fun launchCalculation()
    fun launchCalculationForPeriod(dateFrom: String?, dateTo: String?)
    fun calculateForId(issueId: String, currentIndex: Int, issuesSize: Int, update:Boolean): List<IssueTimelineItem>
}
