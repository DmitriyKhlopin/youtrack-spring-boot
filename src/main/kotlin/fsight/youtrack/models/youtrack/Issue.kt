package fsight.youtrack.models.youtrack

import com.google.gson.Gson
import fsight.youtrack.*
import fsight.youtrack.generated.jooq.tables.records.IssuesRecord
import java.sql.Timestamp
import java.time.LocalDateTime

data class Issue(
        var idReadable: String? = null,
        var created: Long? = null,
        var updated: Long? = null,
        var resolved: Long? = null,
        var numberInProject: Long? = null,
        var project: Project? = null,
        var summary: String? = null,
        var description: String? = null,
        var usesMarkdown: Boolean? = null,
        var wikifiedDescription: String? = null,
        var reporter: User? = null,
        var updater: User? = null,
        var draftOwner: User? = null,
        var isDraft: Boolean? = null,
        var visibility: Visibility? = null,
        var votes: Int? = null,
        var comments: Array<IssueComment>? = null,
        var commentsCount: Long? = null,
        var tags: Array<IssueTag>? = null,
        var links: Array<IssueLink>? = null,
        var externalIssue: ExternalIssue? = null,
        var customFields: Array<SimpleIssueCustomField>? = null,
        var voters: IssueVoters? = null,
        var watchers: Array<IssueWatcher>? = null,
        /*var attachments: Array<Any>?,*/
        var subtasks: Array<IssueLink>? = null,
        var parent: Array<IssueLink>? = null
) {
    fun toIssueRecord(): IssuesRecord {
        //TODO исправить, не работает
        val firstResponseDate = this.unwrapLongValue("Дата первого ответа")
        val solutionDate = this.unwrapLongValue("Дата решения")
        if (this.idReadable == "ITS-44") {
            println(this.customFields)
            println(firstResponseDate)
            println(solutionDate)
        }
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
                unwrapFieldValue("subsystem"),
                unwrapFieldValue("SLA"),
                unwrapFieldValue("SLA по первому ответу"),
                firstResponseDate?.toTimestamp(),
                firstResponseDate?.toDate(),
                firstResponseDate?.toDate(toStartOfTheWeek = true),
                unwrapFieldValue("SLA по решению"),
                solutionDate?.toTimestamp(),
                solutionDate?.toDate(),
                solutionDate?.toDate(toStartOfTheWeek = true),
                project?.name,
                unwrapFieldValue("Type"),
                unwrapFieldValue("State"),
                unwrapFieldValue("Priority"),
                unwrapFieldValue("Версия Prognoz Platform"),
                unwrapFieldValue("Оценка"),
                null,
                null,
                null,
                Timestamp.valueOf(LocalDateTime.now()),
                null,
                null,
                null,
                unwrapFieldValue("Проект (ETS)"),
                project?.shortName,
                unwrapFieldValue("Заказчик")
        )
    }

    fun unwrapLongValue(fieldName: String): Long? {
        val i = this.customFields?.firstOrNull { field -> field.name == fieldName } ?: return null
        val t = (Gson().toJsonTree(i).asJsonObject) ?: return null
        val t2 = t.get("value") ?: return null
        return t2.asLong
    }

    fun unwrapEnumValue(fieldName: String): String? {
        val temp = this.customFields?.firstOrNull { field -> field.name == fieldName }?.value ?: return null
        return (Gson().toJsonTree(temp).asJsonObject).get("name").asString
    }

    fun unwrapFieldValue(fieldName: String?): String? {
        fieldName ?: return null
        val field = this.customFields?.firstOrNull { field -> field.name == fieldName } ?: return null
        return when (field.`$type`) {
            SINGLE_ENUM_ISSUE_CUSTOM_FIELD, SINGLE_OWNED_ISSUE_CUSTOM_FIELD, STATE_ISSUE_CUSTOM_FIELD, STATE_MACHINE_ISSUE_CUSTOM_FIELD, SINGLE_VERSION_ISSUE_CUSTOM_FIELD, SINGLE_BUILD_ISSUE_CUSTOM_FIELD -> {
                val temp = field.value ?: return null
                (Gson().toJsonTree(temp).asJsonObject).get("name").asString
            }
            SINGLE_USER_ISSUE_CUSTOM_FIELD -> {
                val temp = field.value ?: return null
                (Gson().toJsonTree(temp).asJsonObject).get("login").asString
            }
            SIMPLE_ISSUE_CUSTOM_FIELD, DATE_ISSUE_CUSTOM_FIELD -> {
                val t = (Gson().toJsonTree(field).asJsonObject) ?: return null
                val t2 = t.get("value") ?: return null
                t2.asLong.toString()
            }
            MULTI_VERSION_ISSUE_CUSTOM_FIELD, MULTI_ENUM_ISSUE_CUSTOM_FIELD -> {
                val temp = field.value ?: return null
                val arr = Gson().toJsonTree(temp).asJsonArray ?: return null
                if (arr.size() == 0) return null
                arr.joinToString(separator = ", ") { item -> item.asJsonObject.get("name").asString }
            }
            TEXT_ISSUE_CUSTOM_FIELD -> {
                val temp = field.value ?: return null
                (Gson().toJsonTree(temp).asJsonObject).get("text").asString
            }
            PERIOD_ISSUE_CUSTOM_FIELD -> {
                val temp = field.value ?: return null
                (Gson().toJsonTree(temp).asJsonObject).get("minutes").asInt.toString()
            }
            else -> {
                println("${field.`$type`} - ${field.projectCustomField?.field?.name}")
                field.value.toString()
            }
        }
    }
}


