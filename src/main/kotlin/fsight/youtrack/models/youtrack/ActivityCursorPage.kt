package fsight.youtrack.models.youtrack

data class ActivityCursorPage(
        var reverse: Boolean?,
        var beforeCursor: String?,
        var afterCursor: String?,
        var hasBefore: Boolean?,
        var hasAfter: Boolean?,
        var activities: ActivityItem?
)