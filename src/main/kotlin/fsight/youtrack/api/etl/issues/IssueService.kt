package fsight.youtrack.api.etl.issues

interface IssueService {
    fun getIssues(customFilter: String?): Int
    fun deleteIssues(issues: ArrayList<String>): Int
    fun checkIssues()
}