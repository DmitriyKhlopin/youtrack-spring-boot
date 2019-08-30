package fsight.youtrack.models

data class YouTrackCommand(
    var issues: MutableCollection<YouTrackIssue>,
    var silent: Boolean = true,
    var query: String = ""
)
