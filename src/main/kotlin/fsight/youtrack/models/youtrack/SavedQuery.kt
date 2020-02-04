package fsight.youtrack.models.youtrack


data class SavedQuery(
        var query: String?,
        var issues: Array<Issue>?,
        var owner: User?,
        var visibleFor: UserGroup?,
        var updateableBy: UserGroup?,
        var name: String?
)