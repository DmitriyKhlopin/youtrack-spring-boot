package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class IssueWatcher(
        var user: User?,
        var issue: Issue?,
        var isStarred: Boolean?
)