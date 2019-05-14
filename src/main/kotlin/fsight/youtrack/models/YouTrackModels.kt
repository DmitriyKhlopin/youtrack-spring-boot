package fsight.youtrack.models

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import fsight.youtrack.*
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.records.IssueHistoryRecord
import fsight.youtrack.generated.jooq.tables.records.IssuesRecord
import org.jooq.DSLContext
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.math.roundToInt

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
    var project: YouTrackProject? = null
)

data class YouTrackPostableIssue(
    var fields: List<FieldValueBase>? = null,
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
    var project: YouTrackProject? = null
)

data class YouTrackProject(
    var shortName: String? = null,
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var versions: String? = null,
    var subsystems: String? = null,
    var assigneesFullName: String? = null
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

//TODO стоит ли использовать для получения истории?
data class YouTrackProjectCustomField(
    var field: YouTrackCustomField,
    var `$type`: String? = null
)

data class YouTrackCustomField(
    var fieldType: YouTrackFieldType? = null,
    var name: String? = null,
    var `$type`: String? = null
)

data class YouTrackCustomFilterField(
    var customField: YouTrackCustomField? = null,
    var id: String? = null,
    var presentation: String? = null,
    var name: String? = null,
    var `$type`: String? = null
)

data class YouTrackFieldType(
    var isMultiValue: Boolean? = null,
    var valueType: String? = null,
    var `$type`: String? = null
)

data class YouTrackActivityCursor(
    var cursor: String? = null,
    var activities: List<YouTrackActivity>? = null,
    var hasAfter: Boolean? = null,
    var hasBefore: Boolean? = null,
    var `$type`: String
)

data class YouTrackActivity(
    var field: YouTrackCustomFilterField? = null,
    var id: String? = null,
    var target: YouTrackIssue? = null,
    var timestamp: Long? = null,
    var category: Any? = null,
    var removed: Any? = null,
    var added: Any? = null,
    var author: YouTrackUser? = null,
    var targetMember: String? = null,
    var `$type`: String? = null
)

fun YouTrackIssue.toIssueRecord(): IssuesRecord {
    //TODO исправить, не работает
    val firstResponseDate = this.unwrapLongValue("Дата первого ответа")
    val solutionDate = this.unwrapLongValue("Дата решения")
    return IssuesRecord(
        idReadable,
        idReadable,
        summary,
        created?.toTimestamp(),
        created?.toDate(),
        created?.toDate(toStartOfTheWeek = true),
        updated?.toTimestamp(),
        updated.toDate(),
        updated.toDate(toStartOfTheWeek = true),
        resolved?.toTimestamp(),
        resolved?.toDate(),
        resolved?.toDate(toStartOfTheWeek = true),
        reporter?.login,
        comments?.size,
        votes,
        unwrapEnumValue("subsystem"),
        unwrapEnumValue("SLA"),
        unwrapEnumValue("SLA по первому ответу"),
        firstResponseDate?.toTimestamp(),
        firstResponseDate?.toDate(),
        firstResponseDate?.toDate(toStartOfTheWeek = true),
        unwrapEnumValue("SLA по решению"),
        solutionDate?.toTimestamp(),
        solutionDate?.toDate(),
        solutionDate?.toDate(toStartOfTheWeek = true),
        project?.name,
        unwrapEnumValue("Type"),
        unwrapEnumValue("State"),
        unwrapEnumValue("Priority"),
        unwrapEnumValue("Версия Prognoz Platform"),
        unwrapEnumValue("Оценка"),
        null,
        null,
        null,
        Timestamp.valueOf(LocalDateTime.now()),
        null,
        null,
        null,
        unwrapEnumValue("Проект (ETS)"),
        project?.shortName,
        unwrapEnumValue("Заказчик")
    )
}

fun List<IssuesRecord>.loadToDatabase(dslContext: DSLContext): Int {
    dslContext.deleteFrom(ISSUES).where(ISSUES.ID.`in`(this.map { it.id })).execute()
    return dslContext.loadInto(ISSUES).onDuplicateKeyUpdate().loadRecords(this).fields(
        ISSUES.ID,
        ISSUES.ENTITY_ID,
        ISSUES.SUMMARY,
        ISSUES.CREATED_DATE_TIME,
        ISSUES.CREATED_DATE,
        ISSUES.CREATED_WEEK,
        ISSUES.UPDATED_DATE_TIME,
        ISSUES.UPDATED_DATE,
        ISSUES.UPDATED_WEEK,
        ISSUES.RESOLVED_DATE_TIME,
        ISSUES.RESOLVED_DATE,
        ISSUES.RESOLVED_WEEK,
        ISSUES.REPORTER_LOGIN,
        ISSUES.COMMENTS_COUNT,
        ISSUES.VOTES,
        ISSUES.SUBSYSTEM,
        ISSUES.SLA,
        ISSUES.SLA_FIRST_RESPONSE_INDEX,
        ISSUES.SLA_FIRST_RESPONSE_DATE_TIME,
        ISSUES.SLA_FIRST_RESPONSE_DATE,
        ISSUES.SLA_FIRST_RESPONSE_WEEK,
        ISSUES.SLA_SOLUTION_INDEX,
        ISSUES.SLA_SOLUTION_DATE_TIME,
        ISSUES.SLA_SOLUTION_DATE,
        ISSUES.SLA_SOLUTION_WEEK,
        ISSUES.PROJECT,
        ISSUES.ISSUE_TYPE,
        ISSUES.STATE,
        ISSUES.PRIORITY,
        ISSUES.PP_VERSION,
        ISSUES.QUALITY_EVALUATION,
        ISSUES.TIME_USER,
        ISSUES.TIME_AGENT,
        ISSUES.TIME_DEVELOPER,
        ISSUES.LOADED_DATE,
        ISSUES.QUALITY_EVALUATION_DATE_TIME,
        ISSUES.QUALITY_EVALUATION_DATE,
        ISSUES.QUALITY_EVALUATION_WEEK,
        ISSUES.ETS,
        ISSUES.PROJECT_SHORT_NAME,
        ISSUES.CUSTOMER
    ).execute().stored()
}

fun YouTrackActivity.toIssueHistoryRecord(idReadable: String): IssueHistoryRecord {
    val isMultiValue = this.field?.customField?.fieldType?.isMultiValue ?: false
    val type = this.field?.customField?.fieldType?.valueType ?: "string"
    val constAdded = this.added
    val constRemoved = this.removed

    val addedValue: Any? = when {
        !isMultiValue && type == "period" -> (constAdded as? Double)?.roundToInt()
        !isMultiValue && type in listOf("state", "enum", "ownedField") && constAdded is List<*> -> {
            (constAdded.firstOrNull() as? LinkedTreeMap<*, *>)?.get("name") ?: "Undefined state"
        }
        !isMultiValue && type == "date and time" -> (constAdded as? Double)?.toLong()
        !isMultiValue && type in listOf("string", "text") -> constAdded
        !isMultiValue && type == "user" && constAdded is List<*> -> (constAdded.firstOrNull() as? LinkedTreeMap<*, *>)?.get(
            "name"
        ) ?: "Undefined user"
        isMultiValue && type == "version" && constAdded is List<*> -> constAdded.map {
            (it as? LinkedTreeMap<*, *>)?.get(
                "name"
            ) ?: "Undefined version"
        }.toTypedArray().joinToString(separator = ", ")
        else -> null
    }

    val removedValue: Any? = when {
        !isMultiValue && type == "period" -> (constRemoved as? Double)?.roundToInt()
        !isMultiValue && type in listOf("state", "enum", "ownedField") && constRemoved is List<*> -> {
            (constRemoved.firstOrNull() as? LinkedTreeMap<*, *>)?.get("name")
        }
        !isMultiValue && type == "date and time" -> (constRemoved as? Double)?.toLong()
        !isMultiValue && type in listOf("string", "text") -> constRemoved
        !isMultiValue && type == "user" && constRemoved is List<*> -> (constRemoved.firstOrNull() as? LinkedTreeMap<*, *>)?.get(
            "name"
        )
        isMultiValue && type == "version" && constRemoved is List<*> -> constRemoved.mapNotNull {
            (it as? LinkedTreeMap<*, *>)?.get("name")
        }.joinToString(separator = ", ")
        else -> null
    }
    return IssueHistoryRecord(
        idReadable,
        this.author?.email,
        timestamp?.toTimestamp(),
        field?.name ?: "Undefined field",
        type,
        null,
        null,
        removedValue.toString(),
        addedValue.toString(),
        null,
        null,
        timestamp?.toDate(toStartOfTheWeek = true)
    )
}

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
    var groups: List<HubUserGroup>? = null,
    var ringId: String? = null,
    var jabber: String? = null,
    var email: String? = null
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

/*fun YouTrackIssue.unwrapIntValue(fieldName: String): Int? {
    val temp = this.fields?.firstOrNull { it.projectCustomField?.field?.name == fieldName }?.value
    return if (temp != null) (temp as JsonObject).get("name").asInt else null
}*/


fun YouTrackIssue.unwrapLongValue(fieldName: String): Long? = 0
/*fun YouTrackIssue.unwrapStringValue(fieldName: String): String? =
    fields?.firstOrNull { field -> field.projectCustomField?.field?.name == fieldName }?.value.toString()*/

fun YouTrackIssue.unwrapEnumValue(fieldName: String): String? {
    val temp = fields?.firstOrNull { field -> field.projectCustomField?.field?.name == fieldName }?.value ?: return null
    return (Gson().toJsonTree(temp).asJsonObject).get("name").asString
}

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
