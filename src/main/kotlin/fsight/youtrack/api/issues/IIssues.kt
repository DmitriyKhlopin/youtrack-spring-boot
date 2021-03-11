package fsight.youtrack.api.issues

interface IIssues {
    fun getHighPriorityIssuesWithDevOpsDetails(
        projectsString: String?,
        customersString: String?,
        prioritiesString: String?,
        statesString: String?
    ): Any
    fun getIssuesWithTFSDetails(issueFilter: IssueFilter): Any
    fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any
    fun getUnresolved(): List<String>
    fun getIssuesTimelineById(issueId: String): Any
    fun getIssuesDetailedTimelineById(issueId: String): Any
}
