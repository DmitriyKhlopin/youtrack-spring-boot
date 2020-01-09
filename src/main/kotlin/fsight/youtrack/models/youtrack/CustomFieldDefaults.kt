package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class CustomFieldDefaults(
        var canBeEmpty: Boolean?,
        var emptyFieldText: String?,
        var isPublic: Boolean?,
        var parent: CustomField
)