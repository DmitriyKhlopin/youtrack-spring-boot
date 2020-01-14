package fsight.youtrack.etl.issues

import fsight.youtrack.models.youtrack.Issue
import org.jooq.tools.json.JSONObject

interface IIssue {
    fun getIssues(customFilter: String?): Int
    fun deleteIssues(issues: ArrayList<String>): Int
    fun getIssueHistory(idReadable: String)
    fun findDeletedIssues()
    fun checkPendingIssues()
    fun checkIfIssueExists(id: String, filter: String): JSONObject
    fun getIssueById(id: String): Issue
    fun search(filter: String, fields: List<String>): List<Issue>
}
