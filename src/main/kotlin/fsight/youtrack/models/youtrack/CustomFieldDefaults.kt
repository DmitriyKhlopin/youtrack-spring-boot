package fsight.youtrack.models.youtrack

data class CustomFieldDefaults(
        var canBeEmpty: Boolean?,
        var emptyFieldText: String?,
        var isPublic: Boolean?,
        var parent: CustomField
)