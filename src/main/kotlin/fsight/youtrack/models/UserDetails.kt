package fsight.youtrack.models

data class UserDetails(
        var login: String,
        var fullName: String,
        var email: String,
        var jabber: String?,
        var groupsUrl: String,
        var rolesUrl: String
)