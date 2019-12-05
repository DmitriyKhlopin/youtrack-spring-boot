package fsight.youtrack.models.youtrack

data class IssueTag(
        var issues: Array<Issue>?,
        var color: FieldStyle?,
        var untagOnResolve: Boolean?,
        var owner: User?,
        var visibleFor: UserGroup?,
        var updateableBy: UserGroup?,
        var name: String?
)