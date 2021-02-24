package fsight.youtrack.etl.issues

import fsight.youtrack.models.youtrack.Issue
import org.jooq.tools.json.JSONObject

interface IIssue {
    fun getIssues(customFilter: String?): ArrayList<String>
    fun deleteIssues(issues: ArrayList<String>): Int
    fun getSingleIssueHistory(idReadable: String)
    fun getIssuesHistory(ids: ArrayList<String>)
    fun getWorkItems(ids: ArrayList<String>)
    fun findDeletedIssues()
    fun checkUnresolvedIssues()
    fun checkIfIssueExists(id: String, filter: String): JSONObject
    fun getIssueById(id: String): Issue
    fun search(filter: String, fields: List<String>): List<Issue>
    fun updateCumulativeFlow()
    fun updateCumulativeFlowToday()
    fun getIssuesForDetailedTimelineCalculation(): List<String>
    fun calculateDetailedTimeline(): Any
    fun calculateDetailedTimelineById(id: String): Any
}
