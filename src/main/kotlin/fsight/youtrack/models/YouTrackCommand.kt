package fsight.youtrack.models

import fsight.youtrack.api.tfs.TFSDataImplementation

data class YouTrackCommand(var issues: ArrayList<TFSDataImplementation.IssueIn>, var silent: Boolean = false, var query: String = "")