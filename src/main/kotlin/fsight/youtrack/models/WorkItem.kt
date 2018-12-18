package fsight.youtrack.models

data class WorkItem(
    var issueId: String? = null,
    var url: String,
    var id: String,
    var date: Long,
    var created: Long,
    var updated: Long?,
    var duration: Int,
    var author: Author,
    var worktype: WorkType?,
    var description: String?
)
