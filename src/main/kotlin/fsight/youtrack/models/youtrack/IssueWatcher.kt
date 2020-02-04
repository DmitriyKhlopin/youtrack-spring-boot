package fsight.youtrack.models.youtrack


data class IssueWatcher(
        var user: User?,
        var issue: Issue?,
        var isStarred: Boolean?
)