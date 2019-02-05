package fsight.youtrack.etl.issues

import fsight.youtrack.*
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.etl.ETL
import fsight.youtrack.etl.logs.IImportLog
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.ErrorLog.ERROR_LOG
import fsight.youtrack.generated.jooq.tables.IssueComments.ISSUE_COMMENTS
import fsight.youtrack.generated.jooq.tables.IssueHistory.ISSUE_HISTORY
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.models.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class Issue(private val dslContext: DSLContext, private val importLogService: IImportLog) : IIssue {
    private val issueIds = arrayListOf<String?>()

    override fun getIssues(customFilter: String?): Int {
        println("Loading issues")
        val fields =
            listOf(
                "idReadable",
                "reporter(login,name)",
                "updater(login,name)",
                "summary",
                "description",
                "project(shortName)",
                "created",
                "updated",
                "resolved",
                "votes",
                "comments(id,author(login,fullName),text,created,updated,deleted)",
                "fields(\$type,projectCustomField(\$type,field(name)),value(\$type,avatarUrl,buildLink,fullName,id,isResolved,localizedName,login,minutes,name,presentation,ringId,text))",
                "visibility(permittedGroups(name),permittedUsers(name,login))",
                "deleted"
            ).joinToString(",")
        val top = 10
        var i = 1
        var skip = 0
        val filter = customFilter ?: getFilter()
        println("Actual filter: $filter")

        while (i > 0) {
            i = 0
            val j = if (filter == null)
                YouTrackAPI
                    .create(Converter.GSON)
                    .getIssueList(auth = AUTH, fields = fields, top = top, skip = skip)
            else
                YouTrackAPI
                    .create(Converter.GSON)
                    .getIssueList(auth = AUTH, fields = fields, top = top, skip = skip, query = filter)



            j.execute()
                .body()
                ?.forEach {
                    issueIds.add(it.idReadable)
                    skip += 1
                    i += 1
                    it.saveBasicInfo()
                    it.saveComments()
                    it.saveCustomFields()
                }
        }

        /*if (filter == null) {
            while (i > 0) {
                i = 0
                YouTrackAPI
                    .create(Converter.GSON)
                    .getIssueList(auth = AUTH, fields = fields, top = top, skip = skip)
                    .execute()
                    .body()
                    ?.forEach {
                        issueIds.add(it.idReadable)
                        skip += 1
                        i += 1
                        it.saveBasicInfo()
                        it.saveComments()
                        it.saveCustomFields()
                    }
            }
        } else {
            while (i > 0) {
                i = 0
                YouTrackAPI
                    .create(Converter.GSON)
                    .getIssueList(auth = AUTH, fields = fields, top = top, skip = skip, query = filter)
                    .execute()
                    .body()
                    ?.forEach {
                        issueIds.add(it.idReadable)
                        skip += 1
                        i += 1
                        it.saveBasicInfo()
                        it.saveComments()
                        it.saveCustomFields()
                    }
            }
        }*/
        issueIds.filterNotNull().forEach { getTimeAccounting(it) }
        println("Loaded $skip issues")
        importLogService.saveLog(
            ImportLogModel(
                timestamp = Timestamp(System.currentTimeMillis()),
                source = "issues (filtered by '$customFilter')",
                table = "issues",
                items = skip
            )
        )
        issueIds.clear()
        return skip
    }

    private fun getFilter(): String? {
        return try {
            println("waiting for filter")
            val issuesCount = dslContext.selectCount().from(ISSUES).fetchOneInto(Int::class.java)
            val mode = if (issuesCount == 0) IssueRequestMode.ALL else IssueRequestMode.TODAY
            if (mode == 1) {
                val maxUpdateDate =
                    dslContext.select(DSL.max(ISSUES.UPDATED_DATE)).from(ISSUES).fetchOneInto(Timestamp::class.java)
                val dateFrom =
                    "${maxUpdateDate.toLocalDateTime().year}-${if (maxUpdateDate.toLocalDateTime().monthValue < 10) "0" else ""}${maxUpdateDate.toLocalDateTime().monthValue}-${if (maxUpdateDate.toLocalDateTime().dayOfMonth < 10) "0" else ""}${maxUpdateDate.toLocalDateTime().dayOfMonth}"
                val dateTo =
                    "${LocalDate.now().plusDays(1).year}-${if (LocalDate.now().plusDays(1).monthValue < 10) "0" else ""}${LocalDate.now().plusDays(
                        1
                    ).monthValue}-${if (LocalDate.now().plusDays(1).dayOfMonth < 10) "0" else ""}${LocalDate.now().plusDays(
                        1
                    ).dayOfMonth}"
                "updated: $dateFrom .. $dateTo"
            } else null
        } catch (e: Exception) {
            ETL.etlState = ETLState.DONE
            null
        }
    }


    private fun YouTrackIssue.saveBasicInfo() {
        try {
            dslContext.deleteFrom(ISSUES).where(ISSUES.ID.eq(idReadable)).execute()
            dslContext
                .insertInto(ISSUES)
                .set(ISSUES.ID, idReadable)
                .set(ISSUES.ENTITY_ID, idReadable)
                .set(ISSUES.SUMMARY, summary)
                .set(ISSUES.CREATED_DATE_TIME, created?.toTimestamp())
                .set(ISSUES.CREATED_DATE, created?.toDate())
                .set(ISSUES.CREATED_WEEK, created?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.UPDATED_DATE_TIME, updated?.toTimestamp())
                .set(ISSUES.UPDATED_DATE, updated.toDate())
                .set(ISSUES.UPDATED_WEEK, updated.toDate(toStartOfTheWeek = true))
                .set(ISSUES.RESOLVED_DATE_TIME, resolved?.toTimestamp())
                .set(ISSUES.RESOLVED_DATE, resolved?.toDate())
                .set(ISSUES.RESOLVED_WEEK, resolved?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.REPORTER_LOGIN, reporter?.login ?: "undefined")
                .set(ISSUES.COMMENTS_COUNT, comments?.size)
                .set(ISSUES.VOTES, votes)
                .set(ISSUES.SUBSYSTEM, this.unwrapEnumValue("subsystem"))
                .set(ISSUES.SLA, this.unwrapEnumValue("SLA"))
                .set(ISSUES.SLA_FIRST_RESPONSE_INDEX, this.unwrapEnumValue("SLA по первому ответу"))
                .set(ISSUES.SLA_FIRST_RESPONSE_DATE_TIME, this.unwrapLongValue("Дата первого ответа")?.toTimestamp())
                .set(ISSUES.SLA_FIRST_RESPONSE_DATE, this.unwrapLongValue("Дата первого ответа")?.toDate())
                .set(ISSUES.SLA_FIRST_RESPONSE_WEEK, this.unwrapLongValue("Дата первого ответа")?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.SLA_SOLUTION_INDEX, this.unwrapEnumValue("SLA по решению"))
                .set(ISSUES.SLA_SOLUTION_DATE_TIME, this.unwrapLongValue("Дата решения")?.toTimestamp())
                .set(ISSUES.SLA_SOLUTION_DATE, this.unwrapLongValue("Дата решения")?.toDate())
                .set(ISSUES.SLA_SOLUTION_WEEK, this.unwrapLongValue("Дата решения")?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.PROJECT, project?.name)
                .set(ISSUES.ISSUE_TYPE, this.unwrapEnumValue("Type"))
                .set(ISSUES.STATE, this.unwrapEnumValue("State"))
                .set(ISSUES.PRIORITY, this.unwrapEnumValue("Priority"))
                .set(ISSUES.PP_VERSION, this.unwrapEnumValue("Версия Prognoz Platform"))
                .set(ISSUES.QUALITY_EVALUATION, this.unwrapEnumValue("Оценка"))
                .set(ISSUES.ETS, this.unwrapEnumValue("Проект (ETS)"))
                .set(ISSUES.LOADED_DATE, Timestamp.valueOf(LocalDateTime.now()))
                .set(ISSUES.PROJECT_SHORT_NAME, project?.shortName)
                .set(ISSUES.CUSTOMER, this.unwrapEnumValue("Заказчик"))
                .onDuplicateKeyUpdate()
                .set(ISSUES.ENTITY_ID, idReadable)
                .set(ISSUES.SUMMARY, summary)
                .set(ISSUES.CREATED_DATE_TIME, created?.toTimestamp())
                .set(ISSUES.CREATED_DATE, created?.toDate())
                .set(ISSUES.CREATED_WEEK, created?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.UPDATED_DATE_TIME, updated?.toTimestamp())
                .set(ISSUES.UPDATED_DATE, updated.toDate())
                .set(ISSUES.UPDATED_WEEK, updated.toDate(toStartOfTheWeek = true))
                .set(ISSUES.RESOLVED_DATE_TIME, resolved?.toTimestamp())
                .set(ISSUES.RESOLVED_DATE, resolved?.toDate())
                .set(ISSUES.RESOLVED_WEEK, resolved?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.REPORTER_LOGIN, reporter?.login ?: "undefined")
                .set(ISSUES.COMMENTS_COUNT, comments?.size)
                .set(ISSUES.VOTES, votes)
                .set(ISSUES.SUBSYSTEM, this.unwrapEnumValue("subsystem"))
                .set(ISSUES.SLA, this.unwrapEnumValue("SLA"))
                .set(ISSUES.SLA_FIRST_RESPONSE_INDEX, this.unwrapEnumValue("SLA по первому ответу"))
                .set(ISSUES.SLA_FIRST_RESPONSE_DATE_TIME, this.unwrapLongValue("Дата первого ответа")?.toTimestamp())
                .set(ISSUES.SLA_FIRST_RESPONSE_DATE, this.unwrapLongValue("Дата первого ответа")?.toDate())
                .set(ISSUES.SLA_FIRST_RESPONSE_WEEK, this.unwrapLongValue("Дата первого ответа")?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.SLA_SOLUTION_INDEX, this.unwrapEnumValue("SLA по решению"))
                .set(ISSUES.SLA_SOLUTION_DATE_TIME, this.unwrapLongValue("Дата решения")?.toTimestamp())
                .set(ISSUES.SLA_SOLUTION_DATE, this.unwrapLongValue("Дата решения")?.toDate())
                .set(ISSUES.SLA_SOLUTION_WEEK, this.unwrapLongValue("Дата решения")?.toDate(toStartOfTheWeek = true))
                .set(ISSUES.PROJECT, project?.name)
                .set(ISSUES.ISSUE_TYPE, this.unwrapEnumValue("Type"))
                .set(ISSUES.STATE, this.unwrapEnumValue("State"))
                .set(ISSUES.PRIORITY, this.unwrapEnumValue("Priority"))
                .set(ISSUES.PP_VERSION, this.unwrapEnumValue("Версия Prognoz Platform"))
                .set(ISSUES.QUALITY_EVALUATION, this.unwrapEnumValue("Оценка"))
                .set(ISSUES.ETS, this.unwrapEnumValue("Проект (ETS)"))
                .set(ISSUES.LOADED_DATE, Timestamp.valueOf(LocalDateTime.now()))
                .set(ISSUES.PROJECT_SHORT_NAME, project?.shortName)
                .set(ISSUES.CUSTOMER, this.unwrapEnumValue("Заказчик"))
                .execute()
        } catch (e: Exception) {
            ETL.etlState = ETLState.DONE
            writeError(this.toString(), e.message ?: "")
            println(e.message)
        }
    }


    private fun YouTrackIssue.saveCustomFields() {
        dslContext.deleteFrom(CUSTOM_FIELD_VALUES).where(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(idReadable)).execute()
        fields?.forEach { field ->
            try {
                dslContext
                    .insertInto(CUSTOM_FIELD_VALUES)
                    .set(CUSTOM_FIELD_VALUES.ISSUE_ID, idReadable)
                    .set(CUSTOM_FIELD_VALUES.FIELD_NAME, field.projectCustomField?.field?.name)
                    .set(CUSTOM_FIELD_VALUES.FIELD_VALUE, field.unwrapValue())
                    .execute()
            } catch (e: Exception) {
                ETL.etlState = ETLState.DONE
                writeError(field.toString(), e.message ?: "")
            }
        }
    }

    private fun YouTrackIssue.saveComments() {
        dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.eq(idReadable)).execute()
        this.comments?.forEach { comment ->
            dslContext
                .insertInto(ISSUE_COMMENTS)
                .set(ISSUE_COMMENTS.ID, comment.id)
                .set(ISSUE_COMMENTS.ISSUE_ID, this.idReadable)
                .set(ISSUE_COMMENTS.DELETED, comment.deleted)
                .set(ISSUE_COMMENTS.AUTHOR, comment.author?.login)
                .set(ISSUE_COMMENTS.AUTHOR_FULL_NAME, comment.author?.fullName)
                .set(ISSUE_COMMENTS.COMMENT_TEXT, comment.text)
                .set(ISSUE_COMMENTS.CREATED, comment.created?.toTimestamp())
                .set(ISSUE_COMMENTS.UPDATED, comment.updated?.toTimestamp())
                .execute()
            try {

            } catch (e: Exception) {
                ETL.etlState = ETLState.DONE
                writeError(comment.toString(), e.message ?: "")
            }
        }
    }

    private fun getTimeAccounting(issueId: String) {
        dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.eq(issueId)).execute()
        println(issueId)
        YouTrackAPI.create(Converter.GSON).getWorkItems(AUTH, issueId).execute().body()?.forEach {
            dslContext
                .insertInto(WORK_ITEMS)
                .set(WORK_ITEMS.ISSUE_ID, issueId)
                .set(WORK_ITEMS.WI_ID, it.id)
                .set(WORK_ITEMS.WI_DATE, it.date?.toDate())
                .set(WORK_ITEMS.WI_CREATED, it.created?.toTimestamp())
                .set(WORK_ITEMS.WI_UPDATED, it.updated?.toTimestamp())
                .set(WORK_ITEMS.WI_DURATION, it.duration?.minutes)
                .set(WORK_ITEMS.AUTHOR_LOGIN, it.author?.login)
                .set(WORK_ITEMS.WORK_NAME, it.type?.name)
                .set(WORK_ITEMS.WORK_TYPE_ID, it.type?.id)
                .set(WORK_ITEMS.WORK_TYPE_AUTO_ATTACHED, it.type?.autoAttached)
                .set(WORK_ITEMS.DESCRIPTION, it.text)
                .execute()
            try {
            } catch (e: java.lang.Exception) {
                ETL.etlState = ETLState.DONE
                writeError(it.toString(), e.message ?: "")
            }
        }
    }

    /*fun getActivities(items: JsonArray): List<IssueHistoryItem> {
        val activityItems =
            items.filter { obj: JsonElement -> obj.asJsonObject?.get("\$type")?.asString == ACTIVITY_ITEM }

        return activityItems.map { obj: JsonElement ->
            debugPrint(PRINT)
            debugPrint(PRINT, obj.toString())
            val type: String? = obj.asJsonObject?.get("\$type")?.asString
            val target: JsonObject? = obj.asJsonObject?.get("target")?.asJsonObject
            val idReadable = target?.get("idReadable").toString()
            val updateDateTime = obj.asJsonObject?.get("timestamp")?.asLong
            val author: JsonObject? = obj.asJsonObject?.get("author")?.asJsonObject
            val login = author?.get("login").toString()
            val field: JsonObject? = obj.asJsonObject?.get("field")?.asJsonObject
            val fieldName = field?.get("presentation")?.asString
            val customField: JsonObject? = field?.get("customField")?.asJsonObject
            val fieldType: JsonObject? = customField?.get("fieldType")?.asJsonObject
            val valueType: String? = fieldType?.get("valueType")?.asString
            val added: String? = when (valueType) {
                "string", "date and time" -> {
                    val t = obj.asJsonObject?.get("added")
                    if (t is JsonNull) null else obj.asJsonObject?.get("added")?.asString
                }
                "enum", "version", "state" ->
                    obj.asJsonObject?.get("added")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("name")?.asString
                else ->
                    "unwrapped ${obj.asJsonObject?.get("added")?.toString()}"
            }
            val removed: String? = when (valueType) {
                "string", "date and time" -> {
                    val t = obj.asJsonObject?.get("removed")
                    if (t is JsonNull) null else obj.asJsonObject?.get("removed")?.asString
                }
                "enum", "version", "state" ->
                    obj.asJsonObject?.get("removed")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("name")?.asString
                else ->
                    "unwrapped ${obj.asJsonObject?.get("removed")?.toString()}"
            }
            println(valueType)
            println("$type - $added - $removed")

            val result = IssueHistoryItem(
                issueId = idReadable,
                author = login,
                updateDateTime = Timestamp(updateDateTime ?: 0),
                fieldName = fieldName,
                value = null,
                oldValue = removed,
                newValue = added,
                fieldType = valueType
            )
            println(result)
            result
        }
    }

    fun getComments(issueId: String, items: JsonArray): List<Comment> {
        val comments =
            items.filter { obj: JsonElement ->
                obj.asJsonObject?.get("\$type")?.asString in listOf(
                    COMMENT_ACTIVITY_ITEM,
                    MARKUP_ACTIVITY_ITEM
                )
            }
        return comments.map { obj: JsonElement ->
            println()
            println(obj)

            val type: String? = obj.asJsonObject?.get("\$type")?.asString
            val commentId: String = obj.asJsonObject?.get("id")?.asString ?: ""
            val updateDateTime = obj.asJsonObject?.get("timestamp")?.asLong ?: 0
            val author: JsonObject? = obj.asJsonObject?.get("author")?.asJsonObject
            val login = author?.get("login").toString()
            val fullName = author?.get("fullName").toString()
            val added: String = when (type) {
                COMMENT_ACTIVITY_ITEM -> obj.asJsonObject?.get("added")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("text")?.asString
                    ?: ""
                MARKUP_ACTIVITY_ITEM -> obj.asJsonObject?.get("added")?.asString ?: ""
                else -> ""
            }

            val deleted: Boolean = when (type) {
                COMMENT_ACTIVITY_ITEM -> obj.asJsonObject?.get("added")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("deleted")?.asBoolean
                    ?: false
                else -> false
            }
            val comment = Comment(
                id = commentId,
                issueId = issueId,
                parentId = null,
                deleted = deleted,
                shownForIssueAuthor = true,
                author = login,
                authorFullName = fullName,
                text = added,
                created = updateDateTime,
                updated = updateDateTime,
                permittedGroup = "",
                replies = listOf()
            )
            println(comment)
            comment
        }
    }*/

    /*fun getNotUnwrapped(items: JsonArray) {
        val unwrapped =
            items.filter { obj: JsonElement ->
                obj.asJsonObject?.get("\$type")?.asString !in (listOf(
                    ACTIVITY_ITEM,
                    COMMENT_ACTIVITY_ITEM,
                    MARKUP_ACTIVITY_ITEM,
                    TIME_TRACKING_ACTIVITY_ITEM,
                    ATTACHMENT_ACTIVITY_ITEM,
                    ISSUE_CREATED_ACTIVITY_ITEM,
                    PROJECT_ACTIVITY_ITEM,
                    TEXT_FIELD_ACTIVITY_ITEM,
                    VISIBILITY_GROUP_ACTIVITY_ITEM,
                    VISIBILITY_USER_ACTIVITY_ITEM
                ))
            }
        unwrapped.forEach { println(it) }
        println("Not unwrapped ${unwrapped.size} items")
    }

    private fun getIssueHistory(issueId: String) {
        dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.eq(issueId)).execute()
        *//*println(issueId)*//*
        val issueActivities = YouTrackAPI.create(Converter.GSON).getHistory(AUTH, issueId).execute()

        val i = issueActivities.body()?.get("activities") as JsonArray
        val activities = getActivities(i)
        val comments = getComments(issueId, i)
        *//*getNotUnwrapped(i)*//*
        *//*getTimeTracking(issueId, i)*//*
        dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.eq(issueId)).execute()
        comments.forEach { comment ->
            dslContext
                .insertInto(ISSUE_COMMENTS)
                .set(ISSUE_COMMENTS.ID, comment.id)
                .set(ISSUE_COMMENTS.ISSUE_ID, comment.issueId)
                .set(ISSUE_COMMENTS.PARENT_ID, comment.parentId)
                .set(ISSUE_COMMENTS.DELETED, comment.deleted)
                .set(ISSUE_COMMENTS.SHOWN_FOR_ISSUE_AUTHOR, comment.shownForIssueAuthor)
                .set(ISSUE_COMMENTS.AUTHOR, comment.author)
                .set(ISSUE_COMMENTS.AUTHOR_FULL_NAME, comment.authorFullName)
                .set(ISSUE_COMMENTS.COMMENT_TEXT, comment.text)
                .set(ISSUE_COMMENTS.CREATED, comment.created.toTimestamp())
                .set(ISSUE_COMMENTS.UPDATED, comment.updated?.toTimestamp())
                .set(ISSUE_COMMENTS.PERMITTED_GROUP, comment.permittedGroup)
                .set(ISSUE_COMMENTS.REPLIES, comment.replies.toString())
                .execute()
        }


        try {
            val d = dslContext
                .select(DSL.max(ISSUE_HISTORY.UPDATE_DATE_TIME).`as`("nt"))
                .from(ISSUE_HISTORY)
                .where(ISSUE_HISTORY.ISSUE_ID.eq(issueId).and(ISSUE_HISTORY.FIELD_NAME.eq("Оценка")))
                .fetchOneInto(NullableTimestamp::class.java)
            if (d.nt != null)
                dslContext.update(ISSUES)
                    .set(ISSUES.QUALITY_EVALUATION_DATE_TIME, d.nt)
                    .set(ISSUES.QUALITY_EVALUATION_DATE, d.nt.time.toDate())
                    .set(ISSUES.QUALITY_EVALUATION_WEEK, d.nt.time.toDate(toStartOfTheWeek = true))
                    .where(ISSUES.ID.eq(issueId))
                    .execute()
        } catch (e: Exception) {
            ETL.etlState = ETLState.DONE
        }
    }*/

    /* data class NullableTimestamp(
         val nt: Timestamp?
     )*/

    override fun checkIssues() {
        var count = 0
        val deletedItems = arrayListOf<String>()
        val result = dslContext.select(ISSUES.ID).from(ISSUES).fetchInto(String::class.java)
        val interval = (result.size / 100) + 1
        result.forEachIndexed { index, issueId ->
            val requestResult = YouTrackAPI.create(Converter.GSON).getIssueProject(AUTH, issueId).execute()
            val currentProject = requestResult.body()?.project?.shortName
            val prevProject = issueId.substringBefore("-")
            if (currentProject != prevProject) {
                count += 1
                deletedItems.add(issueId)
            }
            if (index % interval == 0) println("Checked ${index * 100 / result.size}% of issues")
        }
        deleteIssues(deletedItems)
    }

    override fun deleteIssues(issues: ArrayList<String>): Int {
        try {
            dslContext.deleteFrom(ISSUES).where(ISSUES.ID.`in`(issues)).execute()
            dslContext.deleteFrom(CUSTOM_FIELD_VALUES).where((CUSTOM_FIELD_VALUES.ISSUE_ID.`in`(issues))).execute()
            dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.`in`(issues)).execute()
            dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.`in`(issues)).execute()
            dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.`in`(issues)).execute()
        } catch (e: java.lang.Exception) {
            ETL.etlState = ETLState.DONE
        }
        return 0
    }

    private fun writeError(item: String, message: String) {
        try {
            dslContext
                .insertInto(ERROR_LOG)
                .set(ERROR_LOG.DATE, Timestamp.valueOf(LocalDateTime.now()))
                .set(ERROR_LOG.ITEM, item)
                .set(ERROR_LOG.ERROR, message)
                .execute()
        } catch (e: Exception) {
            ETL.etlState = ETLState.DONE
        }
    }
}
