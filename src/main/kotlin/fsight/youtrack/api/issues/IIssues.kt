package fsight.youtrack.api.issues

interface IIssues {
    fun getHighPriorityIssuesWithTFSDetails(
        projectsString: String?,
        customersString: String?,
        prioritiesString: String?,
        statesString: String?
    ): Any
}
