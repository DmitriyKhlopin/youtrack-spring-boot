package fsight.youtrack.api.issues

interface IIssues {
    fun getHighPriorityIssuesWithDevOpsDetails(
            projectsString: String?,
            customersString: String?,
            prioritiesString: String?,
            statesString: String?
    ): Any

    fun getIssuesWithTFSDetails(issueFilter: IssueFilter): Any
}
