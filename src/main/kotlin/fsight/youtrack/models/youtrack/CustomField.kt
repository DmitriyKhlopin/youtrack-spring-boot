package fsight.youtrack.models.youtrack

data class CustomField(
        var name: String?,
        var localizedName: String?,
        var fieldType: FieldType?,
        var isAutoAttached: Boolean?,
        var isDisplayedInIssueList: Boolean?,
        var ordinal: Int?,
        var aliases: String?,
        var fieldDefaults: CustomFieldDefaults?,
        var hasRunningJob: Boolean?,
        var isUpdateable: Boolean?,
        var instances: Array<ProjectCustomField>?
)