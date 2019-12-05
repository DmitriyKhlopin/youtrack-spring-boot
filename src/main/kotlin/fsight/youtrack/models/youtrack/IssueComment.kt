package fsight.youtrack.models.youtrack

data class IssueComment(
        var text: String?,
        var usesMarkdown: Boolean?,
        var textPreview: String?,
        var created: Long?,
        var updated: Long?,
        var author: User?,
        var issue: Issue?,
        var attachments: Array<Any>?,
        var visibility: Visibility?,
        var deleted: Boolean?
)