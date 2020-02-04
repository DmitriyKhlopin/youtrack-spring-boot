package fsight.youtrack.models.youtrack


abstract class CustomFieldValueBase

data class SimpleIssueCustomField(
        var projectCustomField: ProjectCustomField?,
        var value: Any?,
        var name: String?,
        var id: String?,
        var `$type`: String?
) : CustomFieldValueBase()

data class LongIssueCustomField(
        var projectCustomField: ProjectCustomField?,
        var value: Any?,
        var name: String?,
        var id: String?,
        var `$type`: String?
) : CustomFieldValueBase()


data class UserCustomFieldValue(
        var name: String? = null,
        val `$type`: String = "User"
)

data class StateCustomFieldValue(
        var name: String? = null,
        val `$type`: String = "StateBundleElement"
)