package fsight.youtrack.models.youtrack

import fsight.youtrack.models.youtrack.Issue

data class Command(
    var issues: MutableCollection<Issue>,
    var silent: Boolean = true,
    var query: String = ""
)
