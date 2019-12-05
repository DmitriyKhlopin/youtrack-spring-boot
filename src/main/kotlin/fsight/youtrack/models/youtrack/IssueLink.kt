package fsight.youtrack.models.youtrack

data class IssueLink(
        var direction: String?,
        var linkType: IssueLinkType?,
        var issues: Array<Issue>?,
        var trimmedIssues: Array<Issue>?
)