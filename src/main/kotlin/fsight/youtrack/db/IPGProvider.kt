package fsight.youtrack.db

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.models.hooks.Hook
import java.sql.Timestamp
import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.YouTrackProject

interface IPGProvider {
    fun saveHookToDatabase(
        body: Hook?,
        fieldState: String?,
        fieldDetailedState: String?,
        errorMessage: String?,
        inferredState: String?,
        commands: ArrayList<String>?,
        type: String,
        rule: ArrayList<Pair<String, Int>>?
    ): Timestamp
    fun getDevOpsAssignees(): List<ETSNameRecord>
    fun getSupportEmployees():List<ETSNameRecord>
    fun getIssueIdsByWIId(id: Int): List<String>
    fun getIssuesIdsInDevelopment(): List<String>
    fun getDynamicsData(projects: String, dateFrom: Timestamp, dateTo: Timestamp): List<Dynamics>
    fun getCommercialProjects(): List<YouTrackProject>
    fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any
    fun updateIssueSpentTimeById(issueId: String?)
}
