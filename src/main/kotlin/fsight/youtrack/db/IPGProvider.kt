package fsight.youtrack.db

import fsight.youtrack.HookTypes
import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.api.issues.IssueWiThDetails
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.records.AreaTeamRecord
import fsight.youtrack.generated.jooq.tables.records.ProductOwnersRecord
import fsight.youtrack.models.*
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.models.web.ComplexAggregatedValue
import fsight.youtrack.models.web.SimpleAggregatedValue1
import fsight.youtrack.models.web.SimpleAggregatedValue2
import java.sql.Timestamp

interface IPGProvider {
    fun saveHookToDatabase(
        body: Hook?,
        fieldState: String? = null,
        fieldDetailedState: String? = null,
        errorMessage: String? = null,
        inferredState: String? = null,
        commands: ArrayList<String>? = null,
        type: HookTypes,
        rule: ArrayList<Pair<String, Int>>? = null
    ): Timestamp

    fun getDevOpsAssignees(): List<ETSNameRecord>
    fun getSupportEmployees(): List<ETSNameRecord>
    fun getIssueIdsByWIId(id: Int): List<String>
    fun getIssuesIdsInDevelopment(): List<String>
    fun getDynamicsData(issueFilter: IssueFilter): List<Dynamics>
    fun getIssuesUpdatedInPeriod(dateFrom: Timestamp, dateTo: Timestamp): List<String>
    fun getCommercialProjects(): List<YouTrackProject>
    fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any
    fun updateIssueSpentTimeById(issueId: String?)
    fun getPartnerCustomers(): List<PartnerCustomerPair>
    fun getYouTrackIssuesWithDetails(issueFilter: IssueFilter): List<IssueWiThDetails>
    fun getAreasWithTeams(): List<AreaTeamRecord>
    fun getProductOwners(): List<ProductOwnersRecord>
    /** Timeline */
    fun updateAllIssuesSpentTime()
    fun getIssueTimelineById(issueId: String): List<IssueTimelineItem>
    fun getIssuesDetailedTimelineById(issueId: String): List<IssueTimelineItem>
    fun saveIssueTimelineItems(items: List<IssueTimelineItem>): Int
    fun saveIssueTimelineDetailedItems(items: List<IssueTimelineItem>): Int
    /** -- */
    fun getInnerProjects(): List<YouTrackProject>
    fun getVelocity(issueFilter: IssueFilter): List<Velocity>
    fun getPrioritiesStats(issueFilter: IssueFilter): List<SimpleAggregatedValue1>
    fun getAverageLifetime(issueFilter: IssueFilter): List<SimpleAggregatedValue1>
    fun getAverageLifetimeUnresolved(issueFilter: IssueFilter): List<SimpleAggregatedValue1>
    fun getSigmaReferenceValues(issueFilter: IssueFilter): List<Int>
    fun getSigmaActualValues(issueFilter: IssueFilter): List<Int>
    fun getCreatedOnWeekByPartner(issueFilter: IssueFilter): List<SimpleAggregatedValue1>
    fun getTypesStats(issueFilter: IssueFilter): List<SimpleAggregatedValue1>
    fun getSLAStatsByPriority(issueFilter: IssueFilter): List<SimpleAggregatedValue2>
    fun getCommercialSLAStatsByPriority(issueFilter: IssueFilter): List<SimpleAggregatedValue2>
    fun getIssuesForDetailedTimelineCalculation(): List<String>
}
