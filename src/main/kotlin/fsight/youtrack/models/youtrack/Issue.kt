package fsight.youtrack.models.youtrack

data class Issue(
        var idReadable: String?,
        var created: Long?,
        var updated: Long?,
        var resolved: Long?,
        var numberInProject: Long?,
        var project: Project?,
        var summary: String?,
        var description: String?,
        var usesMarkdown: Boolean?,
        var wikifiedDescription: String?,
        var reporter: User?,
        var updater: User?,
        var draftOwner: User?,
        var isDraft: Boolean?,
        var visibility: Visibility?,
        var votes: Int?,
        var comments: Array<IssueComment>?,
        var commentsCount: Long?,
        var tags: Array<IssueTag>?,
        var links: Array<IssueLink>?,
        var externalIssue: ExternalIssue?,
        var customFields: Array<IssueCustomField>?,
        var voters: IssueVoters?,
        var watchers: Array<IssueWatcher>?,
        var attachments: Array<Any>?,
        var subtasks: Array<IssueLink>?,
        var parent: Array<IssueLink>?
)


