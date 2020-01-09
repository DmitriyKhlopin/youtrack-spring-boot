package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class ExternalIssue(
        var name: String?,
        var url: String?,
        var key: String?
)