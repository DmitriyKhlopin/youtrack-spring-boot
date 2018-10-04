package fsight.youtrack.issues

interface IssueInterface {
    fun getIssues(): Int
    fun deleteIssues(issues: ArrayList<String>): Int
}