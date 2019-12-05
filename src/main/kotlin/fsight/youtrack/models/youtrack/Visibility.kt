package fsight.youtrack.models.youtrack

data class Visibility(
        var permittedGroups: Array<UserGroup>?,
        var permittedusers: Array<User>?
)