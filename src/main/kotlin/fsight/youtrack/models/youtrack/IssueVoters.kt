package fsight.youtrack.models.youtrack

data class IssueVoters(
        var hasStar: Boolean?,
        var issueWatchers: Array<IssueWatcher>?,
        var duplicateWatchers: Array<IssueWatcher>?
)