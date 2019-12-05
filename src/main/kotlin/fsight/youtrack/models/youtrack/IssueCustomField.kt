package fsight.youtrack.models.youtrack

data class IssueCustomField(
        var projectCustomField: ProjectCustomField?,
        var value: Any?,
        var name: String?,
        var id: String?,
        var `$type`: String?
)