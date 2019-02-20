package fsight.youtrack.etl.timeline

import fsight.youtrack.models.IssueTimelineItem

interface ITimeline {
    fun launchCalculation()
    fun calculateForId(issueId: String): List<IssueTimelineItem>
}
