package fsight.youtrack.etl.issues

import fsight.youtrack.models.YouTrackIssue

interface IIssue {
    fun getIssues(customFilter: String?): Int
    fun deleteIssues(issues: ArrayList<String>): Int
    fun getIssueHistory(idReadable: String)
    fun findDeletedIssues()
    fun checkPendingIssues()
    fun findIssueInYT(id: String, filter: String): Boolean
    fun getIssueById(id: String): YouTrackIssue
}
