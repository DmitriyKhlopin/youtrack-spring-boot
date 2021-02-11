package fsight.youtrack.etl.issues

import fsight.youtrack.*
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.etl.IETLState
import fsight.youtrack.etl.logs.IImportLog
import fsight.youtrack.etl.timeline.ITimeline
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.ErrorLog.ERROR_LOG
import fsight.youtrack.generated.jooq.tables.IssueComments.ISSUE_COMMENTS
import fsight.youtrack.generated.jooq.tables.IssueHistory.ISSUE_HISTORY
import fsight.youtrack.generated.jooq.tables.IssueTags.ISSUE_TAGS
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldValuesRecord
import fsight.youtrack.generated.jooq.tables.records.IssueCommentsRecord
import fsight.youtrack.generated.jooq.tables.records.IssueTagsRecord
import fsight.youtrack.generated.jooq.tables.records.WorkItemsRecord
import fsight.youtrack.models.ImportLogModel
import fsight.youtrack.models.loadToDatabase
import fsight.youtrack.models.toIssueHistoryRecord
import fsight.youtrack.models.youtrack.Issue
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.tools.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class Issue(
    private val dslContext: DSLContext,
    private val importLogService: IImportLog,
    private val etlStateService: IETLState
) : IIssue {
    @Autowired
    private lateinit var pg: IPGProvider

    @Autowired
    private lateinit var timelineService: ITimeline

    private val jsonApi: YouTrackAPI by lazy { YouTrackAPI.create(Converter.GSON) }

    override fun getIssues(customFilter: String?): ArrayList<String> {
        val issueIds = arrayListOf<String>()

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
                "tags(name)",
                "comments(id,author(login,fullName),text,created,updated,deleted)",
                "customFields(\$type,name,projectCustomField(\$type,field(name)),value(\$type,avatarUrl,buildLink,fullName,id,isResolved,localizedName,login,minutes,name,presentation,ringId,text))",
                "visibility(permittedGroups(name),permittedUsers(name,login))",
                "deleted"
            ).joinToString(",")
        val top = 40
        var skip = 0
        var stored = 0
        val filter = customFilter ?: getFilter()
        println("Loading issues with actual filter: $filter")
        do {
            val request = if (filter == null)
                jsonApi.getIssueList(auth = AUTH, fields = fields, top = top, skip = skip)
            else
                jsonApi.getIssueList(auth = AUTH, fields = fields, top = top, skip = skip, query = filter)
            var result: List<Issue>?
            try {
                val t = request.execute()
                result = t.body()
            } catch (e: Exception) {
                writeError("Access error", e.localizedMessage)
                break
            }

            issueIds.addAll(result?.mapNotNull { it.idReadable } ?: listOf())
            skip += result?.size ?: 0
            stored += result?.map { it.toIssueRecord() }?.loadToDatabase(dslContext) ?: 0

            result?.forEach {
                it.saveComments()
                it.saveCustomFields()
                it.saveTags()
                pg.updateIssueSpentTimeById(it.idReadable)
            }
            println("Loaded ${issueIds.size} issues, stored $stored\r")
        } while (result?.size ?: 0 > 0)

        println("Loaded $skip issues\r")
        importLogService.saveLog(
            ImportLogModel(
                timestamp = Timestamp(System.currentTimeMillis()),
                source = "issues (filtered by '$filter')",
                table = "issues",
                items = skip
            )
        )
        return issueIds
    }


    override fun getIssuesHistory(ids: ArrayList<String>) {
        ids.forEach { getSingleIssueHistory(it) }
    }

    override fun getWorkItems(ids: ArrayList<String>) {
        ids.forEach { getSingleIssueWorkItems(it) }
    }

    private fun getFilter(): String? {
        return try {
            val issuesCount = dslContext.selectCount().from(ISSUES).fetchOneInto(Int::class.java)
            val mode = if (issuesCount == 0) IssueRequestMode.ALL else IssueRequestMode.TODAY
            if (mode == 1) {
                val maxUpdateDate =
                    dslContext.select(DSL.max(ISSUES.UPDATED_DATE)).from(ISSUES).fetchOneInto(Timestamp::class.java)
                val dateFrom =
                    "${maxUpdateDate.toLocalDateTime().year}-${if (maxUpdateDate.toLocalDateTime().monthValue < 10) "0" else ""}${maxUpdateDate.toLocalDateTime().monthValue}-${if (maxUpdateDate.toLocalDateTime().dayOfMonth < 10) "0" else ""}${maxUpdateDate.toLocalDateTime().dayOfMonth}"
                val dateTo =
                    "${LocalDate.now().plusDays(1).year}-${if (LocalDate.now().plusDays(1).monthValue < 10) "0" else ""}${
                        LocalDate.now().plusDays(
                            1
                        ).monthValue
                    }-${if (LocalDate.now().plusDays(1).dayOfMonth < 10) "0" else ""}${
                        LocalDate.now().plusDays(
                            1
                        ).dayOfMonth
                    }"
                "updated: $dateFrom .. $dateTo"
            } else null
        } catch (e: Exception) {
            etlStateService.state = ETLState.DONE
            null
        }
    }

    private fun Issue.saveCustomFields() {
        val records = customFields?.map { field ->
            CustomFieldValuesRecord()
                .setIssueId(idReadable)
                .setFieldName(field.projectCustomField?.field?.name)
                .setFieldValue(this.unwrapFieldValue(field.projectCustomField?.field?.name))
        }
        try {
            dslContext.loadInto(CUSTOM_FIELD_VALUES)
                .onDuplicateKeyUpdate()
                .loadRecords(records)
                .fields(
                    CUSTOM_FIELD_VALUES.ISSUE_ID,
                    CUSTOM_FIELD_VALUES.FIELD_NAME,
                    CUSTOM_FIELD_VALUES.FIELD_VALUE
                )
                .execute()

            dslContext.deleteFrom(CUSTOM_FIELD_VALUES)
                .where(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(idReadable))
                .and(CUSTOM_FIELD_VALUES.FIELD_NAME.notIn(customFields?.mapNotNull { field -> field.projectCustomField?.field?.name }))
                .executeAsync()
        } catch (e: Exception) {
            etlStateService.state = ETLState.DONE
            writeError("Custom fields", e.message ?: "")
        }
    }

    private fun Issue.saveComments() {
        val records = this.comments?.map {
            IssueCommentsRecord()
                .setId(it.id)
                .setIssueId(this.idReadable)
                .setDeleted(it.deleted)
                .setAuthor(it.author?.login)
                .setAuthorFullName(it.author?.fullName)
                .setCommentText(it.text)
                .setCreated(it.created?.toTimestamp())
                .setUpdated(it.updated?.toTimestamp())
        }
        try {
            dslContext.loadInto(ISSUE_COMMENTS)
                .onDuplicateKeyUpdate()
                .loadRecords(records)
                .fields(
                    ISSUE_COMMENTS.ID,
                    ISSUE_COMMENTS.ISSUE_ID,
                    ISSUE_COMMENTS.PARENT_ID,
                    ISSUE_COMMENTS.DELETED,
                    ISSUE_COMMENTS.SHOWN_FOR_ISSUE_AUTHOR,
                    ISSUE_COMMENTS.AUTHOR,
                    ISSUE_COMMENTS.AUTHOR_FULL_NAME,
                    ISSUE_COMMENTS.COMMENT_TEXT,
                    ISSUE_COMMENTS.CREATED,
                    ISSUE_COMMENTS.UPDATED,
                    ISSUE_COMMENTS.PERMITTED_GROUP,
                    ISSUE_COMMENTS.REPLIES
                )
                .execute()
            /**
             * Подчищаем удалённые комментарии
             * */
            dslContext.deleteFrom(ISSUE_COMMENTS)
                .where(ISSUE_COMMENTS.ISSUE_ID.eq(idReadable))
                .and(ISSUE_COMMENTS.ID.notIn(this.comments?.mapNotNull { c -> c.id }))
                .executeAsync()
        } catch (e: Exception) {
            etlStateService.state = ETLState.DONE
            writeError("Comments", e.message ?: "")
        }

    }

    private fun Issue.saveTags() {
        dslContext.deleteFrom(ISSUE_TAGS).where(ISSUE_TAGS.ISSUE_ID.eq(idReadable)).execute()
        val tags = this.tags?.map { item -> IssueTagsRecord().setIssueId(this.idReadable).setTag(item.name) }
        try {
            dslContext.loadInto(ISSUE_TAGS).loadRecords(tags).fields(ISSUE_TAGS.ISSUE_ID, ISSUE_TAGS.TAG).execute()
        } catch (e: Exception) {
            etlStateService.state = ETLState.DONE
            writeError("Tags for issue ${this.idReadable}", e.message ?: "")
        }
    }

    private fun getSingleIssueWorkItems(idReadable: String) {
        dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.eq(idReadable)).execute()
        //TODO заменить на loadInto
        val items = jsonApi.getWorkItems(AUTH, idReadable).execute().body()?.map {
            WorkItemsRecord()
                .setIssueId(idReadable)
                .setWiId(it.id)
                .setWiDate(it.date?.toDate())
                .setWiCreated(it.created?.toTimestamp())
                .setWiUpdated(it.updated?.toTimestamp())
                .setWiDuration(it.duration?.minutes)
                .setAuthorLogin(it.author?.login)
                .setWorkName(it.type?.name)
                .setWorkTypeId(it.type?.id)
                .setWorkTypeAutoAttached(it.type?.autoAttached)
                .setDescription(it.text)
        }
        if (items?.size ?: 0 > 0) {
            try {
                dslContext
                    .loadInto(WORK_ITEMS)
                    .onDuplicateKeyUpdate()
                    .loadRecords(items)
                    .fields(
                        WORK_ITEMS.ISSUE_ID,
                        WORK_ITEMS.WI_URL,
                        WORK_ITEMS.WI_ID,
                        WORK_ITEMS.WI_DATE,
                        WORK_ITEMS.WI_CREATED,
                        WORK_ITEMS.WI_UPDATED,
                        WORK_ITEMS.WI_DURATION,
                        WORK_ITEMS.AUTHOR_LOGIN,
                        WORK_ITEMS.AUTHOR_RING_ID,
                        WORK_ITEMS.AUTHOR_URL,
                        WORK_ITEMS.WORK_NAME,
                        WORK_ITEMS.WORK_TYPE_ID,
                        WORK_ITEMS.WORK_TYPE_AUTO_ATTACHED,
                        WORK_ITEMS.WORK_TYPE_URL,
                        WORK_ITEMS.DESCRIPTION
                    )
                    .execute()
            } catch (e: java.lang.Exception) {
                etlStateService.state = ETLState.DONE
                writeError("Can't save work items for issue $idReadable", e.message ?: "")
            }
        }
    }

    override fun getSingleIssueHistory(idReadable: String) {
        println("Loading history of $idReadable")
        try {
            var hasAfter: Boolean? = true
            var offset = 100
            while (hasAfter == true) {
                val issueActivities = jsonApi
                    .getCustomFieldsHistory(auth = AUTH, issueId = idReadable, top = offset)
                    .execute()
                val items = issueActivities.body()?.activities?.map { it.toIssueHistoryRecord(idReadable) }
                val stored = dslContext.loadInto(ISSUE_HISTORY)
                    .onDuplicateKeyUpdate()
                    .loadRecords(items)
                    .fields(
                        ISSUE_HISTORY.ISSUE_ID,
                        ISSUE_HISTORY.AUTHOR,
                        ISSUE_HISTORY.UPDATE_DATE_TIME,
                        ISSUE_HISTORY.FIELD_NAME,
                        ISSUE_HISTORY.VALUE_TYPE,
                        ISSUE_HISTORY.OLD_VALUE_INT,
                        ISSUE_HISTORY.NEW_VALUE_INT,
                        ISSUE_HISTORY.OLD_VALUE_STRING,
                        ISSUE_HISTORY.NEW_VALUE_STRING,
                        ISSUE_HISTORY.OLD_VALUE_DATE_TIME,
                        ISSUE_HISTORY.NEW_VALUE_DATE_TIME,
                        ISSUE_HISTORY.UPDATE_WEEK
                    )
                    .execute()
                    .stored()
                offset += issueActivities.body()?.activities?.size ?: 0
                hasAfter = issueActivities.body()?.hasAfter
                println("$idReadable: stored $stored history items")
            }
        } catch (e: java.lang.Exception) {
            etlStateService.state = ETLState.DONE
            writeError("Can't save work items for issue $idReadable", e.message ?: "")
        }
    }

    override fun findDeletedIssues() {
        try {
            var count = 0
            val deletedItems = arrayListOf<String>()
            val result = dslContext.select(ISSUES.ID).from(ISSUES).fetchInto(String::class.java)
            val interval = (result.size / 100) + 1
            result.forEachIndexed { index, issueId ->
                val requestResult = jsonApi.getIssueProject(AUTH, issueId).execute()
                val currentProject = requestResult.body()?.project?.shortName
                val prevProject = issueId.substringBefore("-")
                if (currentProject != prevProject) {
                    count += 1
                    deletedItems.add(issueId)
                }
                if (index % interval == 0) print("Checked ${index * 100 / result.size}% of issues\r")
            }
            println("Found ${deletedItems.size} deleted issues")
            deleteIssues(deletedItems)
        } catch (e: Exception) {
            etlStateService.state = ETLState.DONE
            writeError("Deleted and moved issues search", e.message ?: "")
        }
    }

    override fun deleteIssues(issues: ArrayList<String>): Int {
        try {
            dslContext.deleteFrom(ISSUES).where(ISSUES.ID.`in`(issues)).execute()
            dslContext.deleteFrom(CUSTOM_FIELD_VALUES).where((CUSTOM_FIELD_VALUES.ISSUE_ID.`in`(issues))).execute()
            dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.`in`(issues)).execute()
            dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.`in`(issues)).execute()
            dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.`in`(issues)).execute()
        } catch (e: java.lang.Exception) {
            etlStateService.state = ETLState.DONE
        }
        return 0
    }

    override fun checkPendingIssues() {
        try {
            val i = dslContext
                .select(ISSUES.ID)
                .from(ISSUES)
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("State")))
                .where(CUSTOM_FIELD_VALUES.FIELD_VALUE.`in`(listOf("Ожидает ответа", "Ожидает подтверждения")))
                /*.and(ISSUES.PROJECT_SHORT_NAME.notIn(listOf("TC")))*/
                .fetchInto(String::class.java)
            println("Found ${i.size} pending issues")
            i.forEach { getIssues(it) }
        } catch (e: Exception) {
            println(e)
        }
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
            etlStateService.state = ETLState.DONE
        }
    }

    override fun getIssueById(id: String): Issue {
        return Issue()
    }

    override fun checkIfIssueExists(id: String, filter: String): JSONObject {
        val fields = "idReadable"
        val composedFilter = "$id $filter".removeSurrounding(" ", " ")
        println(composedFilter)
        val request = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = fields, top = 100, skip = 0, query = composedFilter)
        val result = request.execute().body()?.mapNotNull { it.idReadable }?.contains(id) ?: false
        return JSONObject(mapOf("issueExists" to result))
    }


    override fun search(filter: String, fields: List<String>): List<Issue> {
        val fieldsString = (if (fields.isEmpty()) listOf("idReadable", "customFields(name,value)") else fields).joinToString(",")
        val request = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = fieldsString, top = 100, skip = 0, query = filter)
        return request.execute().body().orEmpty()
    }

    override fun updateCumulativeFlow() {
        try {
            dslContext.execute(
                """
                insert into cumulative_flow_data (d, id, state)
                    select d, id, new_value_string as state
                from cumulative_flow
                    on conflict (d, id) do update set state = excluded.state
            """.trimIndent()
            )
        } catch (e: Exception) {
            writeError("Cumulative flow: today", e.message ?: "")
        }
    }


    override fun updateCumulativeFlowToday() {
        try {
            dslContext.execute(
                """
                insert into cumulative_flow_data (d, id, state)
                    select d, id, new_value_string as state
                from cumulative_flow_today
                    on conflict (d, id) do update set state = excluded.state;
            """.trimIndent()
            )
        } catch (e: Exception) {
            writeError("Cumulative flow: today", e.message ?: "")
        }
    }


    override fun calculateDetailedTimeline(): Any {
        val i =  pg.getIssuesForDetailedTimelineCalculation().map { it to timelineService.calculateDetailedStateByIssueId(it) }

        return i
    }

    override fun calculateDetailedTimelineById(id: String): Any {
        return timelineService.calculateDetailedStateByIssueId(id)
    }

    override fun getIssuesForDetailedTimelineCalculation(): List<String> {
        return pg.getIssuesForDetailedTimelineCalculation()
    }
}
