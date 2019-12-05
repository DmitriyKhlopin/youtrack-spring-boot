package fsight.youtrack.models.youtrack

data class ActivityItem(
        var author: User?,
        var timestamp: Long?,
        var removed: Any?,
        var added: Any?,
        var target: Any?,
        var targetMember: String?,
        var field: FilterField?,
        var category: Any?
)