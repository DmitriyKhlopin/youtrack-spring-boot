package fsight.youtrack.etl.issues

interface IIssue {
    fun getIssues(customFilter: String?): Int
    fun deleteIssues(issues: ArrayList<String>): Int
    fun checkIssues()
}