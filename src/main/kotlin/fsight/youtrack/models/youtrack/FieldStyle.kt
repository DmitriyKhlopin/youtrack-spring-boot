package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class FieldStyle(
        var background: String?,
        var foreground: String?
)