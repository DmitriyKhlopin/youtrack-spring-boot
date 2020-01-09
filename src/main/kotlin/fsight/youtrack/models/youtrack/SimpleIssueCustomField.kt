package fsight.youtrack.models.youtrack

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class SimpleIssueCustomField(
        var projectCustomField: ProjectCustomField?,
        @ContextualSerialization
        var value: CustomFieldValueBase?,
        var name: String?,
        var id: String?,
        var `$type`: String?
)

@Serializable
abstract class CustomFieldValueBase

@Serializable
data class UserCustomFieldValue(
        var name: String? = null,
        val `$type`: String = "User"
) : CustomFieldValueBase()

@Serializable
data class StateCustomFieldValue(
        var name: String? = null,
        val `$type`: String = "StateBundleElement"
) : CustomFieldValueBase()