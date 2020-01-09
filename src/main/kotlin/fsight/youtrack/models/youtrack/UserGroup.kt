package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class UserGroup(
        var name: String?,
        var ringId: String?,
        var usersCount: Long?,
        var icon: String?,
        var allUsersGroup: Boolean?,
        var teamForProject: Project
)