package fsight.youtrack.db

import fsight.youtrack.HookTypes
import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.api.issues.IssueWiThDetails
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.records.AreaTeamRecord
import fsight.youtrack.generated.jooq.tables.records.ProductOwnersRecord
import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.IssueTimelineItem
import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject
import fsight.youtrack.models.hooks.Hook
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
    fun getDynamicsData(projects: String, dateFrom: Timestamp, dateTo: Timestamp): List<Dynamics>
    fun getIssuesUpdatedInPeriod(dateFrom: Timestamp, dateTo: Timestamp): List<String>
    fun getCommercialProjects(): List<YouTrackProject>
    fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any
    fun updateIssueSpentTimeById(issueId: String?)
    fun getPartnerCustomers(): List<PartnerCustomerPair>
    fun getYouTrackIssuesWithDetails(issueFilter: IssueFilter): List<IssueWiThDetails>
    fun getAreasWithTeams(): List<AreaTeamRecord>
    fun getProductOwners(): List<ProductOwnersRecord>
    fun updateAllIssuesSpentTime()
    fun getIssueTimelineItemsById(issueId: String): List<IssueTimelineItem>
    fun saveIssueTimelineItems(items: List<IssueTimelineItem>): Int
}
