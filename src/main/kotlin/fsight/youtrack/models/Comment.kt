package fsight.youtrack.models

data class Comment(
    var id: String,
    var issueId: String,
    var parentId: String?,
    var deleted: Boolean?,
    var shownForIssueAuthor: Boolean?,
    var author: String,
    var authorFullName: String,
    var text: String,
    var created: Long,
    var updated: Long?,
    var permittedGroup: String?,
    var replies: List<String>?
)
