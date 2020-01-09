package fsight.youtrack.models.youtrack

import kotlinx.serialization.Serializable

@Serializable
data class ProjectCustomField(
        var field: CustomField?,
        var project: Project?,
        var canBeEmpty: Boolean?,
        var emptyFieldText: String?,
        var ordinal: Int?,
        var isPublic: Boolean?,
        var hasRunningJob: Boolean?
)