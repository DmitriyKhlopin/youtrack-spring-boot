package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class IssueVoters(
        var hasStar: Boolean?,
        var issueWatchers: Array<IssueWatcher>?,
        var duplicateWatchers: Array<IssueWatcher>?
)