package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class User(
        var login: String,
        var fullName: String,
        var email: String,
        var jabberAccountName: String,
        var ringId: String,
        var guest: Boolean,
        var online: Boolean,
        var banned: Boolean,
        var tags: Array<IssueTag>,
        var savedQueries: Array<SavedQuery>,
        var avatarUrl: String/*,
        var profiles: Any?*/
)