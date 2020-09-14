package fsight.youtrack.models.youtrack


data class CustomField(
    var name: String? = null,
    var id: String? = null,
    var localizedName: String? = null,
    var fieldType: FieldType? = null,
    var isAutoAttached: Boolean? = null,
    var isDisplayedInIssueList: Boolean? = null,
    var ordinal: Int? = null,
    var aliases: String? = null,
    var fieldDefaults: CustomFieldDefaults? = null,
    var hasRunningJob: Boolean? = null,
    var isUpdateable: Boolean? = null,
    var instances: Array<ProjectCustomField>? = null,
    var `$type`: String? = null,
    var valueType: String? = null
)

fun CustomField.isValid(): Boolean = this.id != null && this.name != null && this.valueType != null
