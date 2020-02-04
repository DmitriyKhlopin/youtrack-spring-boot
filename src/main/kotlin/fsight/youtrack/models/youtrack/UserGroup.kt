package fsight.youtrack.models.youtrack

data class UserGroup(
        var name: String?,
        var ringId: String?,
        var usersCount: Long?,
        var icon: String?,
        var allUsersGroup: Boolean?,
        var teamForProject: Project
)