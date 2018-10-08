package fsight.youtrack.etl.issues

interface IssueService {
    fun getIssues(): Int
    fun deleteIssues(issues: ArrayList<String>): Int
}