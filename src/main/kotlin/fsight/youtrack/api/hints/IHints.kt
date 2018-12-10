package fsight.youtrack.api.hints

interface IHints {
    fun getNewIssueHints(project: String?, customer: String?):List<Hints.CustomerRepository>
}