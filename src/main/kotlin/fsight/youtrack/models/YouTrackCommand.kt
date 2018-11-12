package fsight.youtrack.models

data class YouTrackCommand(var issues: MutableCollection<YouTrackIssue>,
                           var silent: Boolean = false,
                           var query: String = "")