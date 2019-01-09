package fsight.youtrack.models

import com.google.gson.Gson
import com.google.gson.JsonObject
import fsight.youtrack.*
import java.sql.Timestamp

data class YouTrackIssue(
    var fields: List<YouTrackField>? = null,
    var description: String? = null,
    var visibility: YouTrackIssueVisibility? = null,
    var summary: String? = null,
    var reporter: YouTrackUser? = null,
    var created: Long? = null,
    var updated: Long? = null,
    var resolved: Long? = null,
    var idReadable: String? = null,
    var updater: YouTrackUser? = null,
    var comments: List<YouTrackComment>? = null,
    var votes: Int? = null,
    var project: Project? = null
)

data class Project(
    var shortName: String? = null,
    var id: String? = null,
    var name: String? = null
)

data class YouTrackComment(
    var id: String? = null,
    var author: YouTrackUser? = null,
    var text: String? = null,
    var created: Long? = null,
    var updated: Long? = null,
    var deleted: Boolean? = null

)

data class YouTrackField(
    var projectCustomField: YouTrackProjectCustomField? = null,
    var value: Any? = null,
    var `$type`: String? = null
)

data class YouTrackProjectCustomField(
    var field: YouTrackCustomField,
    var `$type`: String? = null
)

data class YouTrackCustomField(
    var name: String? = null,
    var `$type`: String? = null
)

data class YouTrackIssueVisibility(
    var permittedGroups: List<Any>? = null,
    var permittedUsers: List<Any>? = null,
    var `$type`: String? = null
)

data class YouTrackUser(
    var type: String? = null,
    var id: String? = null,
    var profile: HubUserProfile? = null,
    var name: String? = null,
    var login: String? = null,
    var fullName: String? = null,
    var `$type`: String? = null,
    var groups: List<HubUserGroup>? = null
)

data class HubUserProfile(
    var email: HubUserEmail? = null
)

data class HubUserEmail(
    var type: String? = null,
    var verified: Boolean? = null,
    var email: String? = null
)

data class HubUserGroup(
    var type: String? = null,
    var name: String? = null, // Group name
    var email: String? = null // User email
)

data class YouTrackIssueWorkItem(
    var created: Long? = null,
    var date: Long? = null,
    var duration: YouTrackPeriodValue? = null,
    var issue: YouTrackIssue? = null,
    var updated: Long? = null,
    var author: YouTrackUser? = null,
    var creator: YouTrackUser? = null,
    var id: String? = null,
    var text: String? = null,
    var type: YouTrackWorkItemType? = null
)

data class YouTrackWorkItemType(
    var id: String? = null,
    var name: String? = null,
    var autoAttached: Boolean? = null
)

data class YouTrackPeriodValue(
    var minutes: Int? = null
)

fun YouTrackIssue.unwrapIntValue(fieldName: String): Int? {
    val temp = this.fields?.firstOrNull { it.projectCustomField?.field?.name == fieldName }?.value
    return if (temp != null) (temp as JsonObject).get("name").asInt else null
}


fun YouTrackIssue.unwrapLongValue(fieldName: String): Long? = 0
fun YouTrackIssue.unwrapStringValue(fieldName: String): String? =
    fields?.firstOrNull { field -> field.projectCustomField?.field?.name == fieldName }?.value.toString()

fun YouTrackIssue.unwrapEnumValue(fieldName: String): String? {
    val temp = fields?.firstOrNull { field -> field.projectCustomField?.field?.name == fieldName }?.value ?: return null
    return (Gson().toJsonTree(temp).asJsonObject).get("name").asString
}

fun YouTrackIssue.unwrapTimestampValue(fieldName: String): Timestamp = Timestamp(0)


fun YouTrackIssue.getFieldValue() = ""

fun YouTrackField.unwrapValue(): String? {
    return when (this.`$type`) {
        SINGLE_ENUM_ISSUE_CUSTOM_FIELD, SINGLE_OWNED_ISSUE_CUSTOM_FIELD, STATE_ISSUE_CUSTOM_FIELD, STATE_MACHINE_ISSUE_CUSTOM_FIELD, SINGLE_VERSION_ISSUE_CUSTOM_FIELD, SINGLE_BUILD_ISSUE_CUSTOM_FIELD -> {
            val temp = this.value ?: return null
            (Gson().toJsonTree(temp).asJsonObject).get("name").asString
        }
        SINGLE_USER_ISSUE_CUSTOM_FIELD -> {
            val temp = this.value ?: return null
            (Gson().toJsonTree(temp).asJsonObject).get("login").asString
        }
        SIMPLE_ISSUE_CUSTOM_FIELD, DATE_ISSUE_CUSTOM_FIELD -> {
            this.value ?: return null
            this.value.toString()
        }
        MULTI_VERSION_ISSUE_CUSTOM_FIELD, MULTI_ENUM_ISSUE_CUSTOM_FIELD -> {
            val temp = this.value ?: return null
            (Gson().toJsonTree(temp).asJsonArray).map { item -> item.asJsonObject.get("name") }
                .joinToString(separator = ", ")
        }
        TEXT_ISSUE_CUSTOM_FIELD -> {
            val temp = this.value ?: return null
            (Gson().toJsonTree(temp).asJsonObject).get("text").asString
        }
        PERIOD_ISSUE_CUSTOM_FIELD -> {
            val temp = this.value ?: return null
            (Gson().toJsonTree(temp).asJsonObject).get("minutes").asInt.toString()
        }
        else -> {
            println("${this.`$type`} - ${this.projectCustomField?.field?.name}")
            this.value.toString()
        }
    }
}
