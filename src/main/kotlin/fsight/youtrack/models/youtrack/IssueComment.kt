package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class IssueComment(
        var id: String? = null,
        var text: String?,
        var usesMarkdown: Boolean?,
        var textPreview: String?,
        var created: Long?,
        var updated: Long?,
        var author: User?,
        var issue: Issue?,
        /*var attachments: Array<Any>?,*/
        var visibility: Visibility?,
        var deleted: Boolean?
)