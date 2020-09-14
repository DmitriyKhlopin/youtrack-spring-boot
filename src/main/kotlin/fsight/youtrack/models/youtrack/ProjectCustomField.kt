package fsight.youtrack.models.youtrack

import fsight.youtrack.models.BundleInstance


data class ProjectCustomField(
    var field: CustomField?,
    var project: Project?,
    var canBeEmpty: Boolean?,
    var emptyFieldText: String?,
    var id: String?,
    var ordinal: Int?,
    var isPublic: Boolean?,
    var hasRunningJob: Boolean?,
    val bundle: BundleInstance?,
    var `$type`: String?
)
