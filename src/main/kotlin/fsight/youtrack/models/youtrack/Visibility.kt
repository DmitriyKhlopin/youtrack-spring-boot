package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class Visibility(
        var permittedGroups: Array<UserGroup>?,
        var permittedusers: Array<User>?
)