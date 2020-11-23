package fsight.youtrack.api.charts

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.SigmaResult
import fsight.youtrack.models.web.SimpleAggregatedValue1
import fsight.youtrack.models.web.SimpleAggregatedValue2

interface IChartData {
    fun getDynamicsData(issueFilter: IssueFilter): List<Dynamics>
    fun getSigmaData(issueFilter: IssueFilter): SigmaResult
    fun getCreatedCountOnWeek(issueFilter: IssueFilter): List<SimpleAggregatedValue1>?
    fun getProcessedDaily(projects: String, dateFrom: String, dateTo: String): Any
    fun getPrioritiesStats(issueFilter: IssueFilter): Any
    fun getAverageLifetime(issueFilter: IssueFilter): Any
    fun getAverageLifetimeUnresolved(issueFilter: IssueFilter): Any
    fun getTypesStats(issueFilter: IssueFilter): Any
    fun getSLAStatsByPriority(issueFilter: IssueFilter): Any
    fun getCommercialSLAStatsByPriority(issueFilter: IssueFilter): List<SimpleAggregatedValue2>
}
