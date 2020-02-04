package fsight.youtrack.models.youtrack

data class Issue(
        var idReadable: String? = null,
        var created: Long? = null,
        var updated: Long? = null,
        var resolved: Long? = null,
        var numberInProject: Long? = null,
        var project: Project? = null,
        var summary: String? = null,
        var description: String? = null,
        var usesMarkdown: Boolean? = null,
        var wikifiedDescription: String? = null,
        var reporter: User? = null,
        var updater: User? = null,
        var draftOwner: User? = null,
        var isDraft: Boolean? = null,
        var visibility: Visibility? = null,
        var votes: Int? = null,
        var comments: Array<IssueComment>? = null,
        var commentsCount: Long? = null,
        var tags: Array<IssueTag>? = null,
        var links: Array<IssueLink>? = null,
        var externalIssue: ExternalIssue? = null,
        var customFields: Array<SimpleIssueCustomField>? = null,
        var voters: IssueVoters? = null,
        var watchers: Array<IssueWatcher>? = null,
        /*var attachments: Array<Any>?,*/
        var subtasks: Array<IssueLink>? = null,
        var parent: Array<IssueLink>? = null
)


