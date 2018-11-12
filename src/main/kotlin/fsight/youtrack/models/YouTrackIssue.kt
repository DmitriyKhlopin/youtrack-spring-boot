package fsight.youtrack.models

import fsight.youtrack.models.v2.Project

data class YouTrackIssue(var description: String? = null,
                         var fields: List<Any>? = null,
                         var project: Project? = null,
                         var summary: String? = null,
                         var id: String? = null,
                         var idReadable: String? = null)