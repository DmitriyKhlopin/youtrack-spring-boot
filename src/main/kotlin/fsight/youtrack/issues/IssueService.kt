package fsight.youtrack.issues

import fsight.youtrack.*
import fsight.youtrack.logs.ImportLogService
import fsight.youtrack.models.ImportLogModel
import fsight.youtrack.models.Issue
import fsight.youtrack.models.sql.IssueHistoryItem
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youtrack.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import youtrack.jooq.tables.ErrorLog.ERROR_LOG
import youtrack.jooq.tables.IssueComments.ISSUE_COMMENTS
import youtrack.jooq.tables.IssueHistory.ISSUE_HISTORY
import youtrack.jooq.tables.Issues.ISSUES
import youtrack.jooq.tables.WorkItems.WORK_ITEMS
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class IssueService(private val dslContext: DSLContext, private val importLogService: ImportLogService) : IssueInterface {
    override fun getIssues(): Int {
        val max = 10
        var i = 1
        var skip = 0
        val filter = getFilter()
        if (filter == null) {
            while (i > 0) {
                i = 0
                AllIssueRetrofitService.create().getIssueList(AUTH, null, skip, max).execute().body()?.issue?.forEach {
                    println(it.id)
                    skip += 1
                    i += 1
                    it.saveBasicInfo()
                    it.saveCustomFields()
                    it.getComments()
                    getTimeAccounting(it.id)
                    getIssueHistory(it.id)
                }
            }
        } else {
            while (i > 0) {
                i = 0
                IssueRetrofitService.create().getIssueList(AUTH, getFilter(), null, skip, max).execute().body()?.issue?.forEach {
                    println(it.id)
                    skip += 1
                    i += 1
                    it.saveBasicInfo()
                    it.saveCustomFields()
                    it.getComments()
                    getTimeAccounting(it.id)
                    getIssueHistory(it.id)
                }
            }
        }
        println("Loaded $skip issues")
        importLogService.saveLog(ImportLogModel(
                timestamp = Timestamp(System.currentTimeMillis()),
                source = "issues (filtered by '$filter')",
                table = "issues",
                items = skip))
        return skip
    }

    private fun getFilter(): String? {
        val issuesCount = dslContext.selectCount().from(ISSUES).fetchOneInto(Int::class.java)
        val mode = if (issuesCount == 0) IssueRequestMode.ALL else IssueRequestMode.TODAY

        return if (mode == 1) {
            val maxUpdateDate = dslContext.select(DSL.max(ISSUES.UPDATED_DATE)).from(ISSUES).fetchOneInto(Timestamp::class.java)
            val dateFrom = "${maxUpdateDate.toLocalDateTime().year}-${if (maxUpdateDate.toLocalDateTime().monthValue < 10) "0" else ""}${maxUpdateDate.toLocalDateTime().monthValue}-${if (maxUpdateDate.toLocalDateTime().dayOfMonth < 10) "0" else ""}${maxUpdateDate.toLocalDateTime().dayOfMonth}"
            val dateTo = "${LocalDate.now().plusDays(1).year}-${if (LocalDate.now().plusDays(1).monthValue < 10) "0" else ""}${LocalDate.now().plusDays(1).monthValue}-${if (LocalDate.now().plusDays(1).dayOfMonth < 10) "0" else ""}${LocalDate.now().plusDays(1).dayOfMonth}"
            "updated: $dateFrom .. $dateTo"
        } else null
    }

    private fun Issue.saveBasicInfo() {
        try {
            dslContext.deleteFrom(ISSUES).where(ISSUES.ID.eq(id)).execute()
            dslContext
                    .insertInto(ISSUES)
                    .set(ISSUES.ID, id)
                    .set(ISSUES.ENTITY_ID, entityId)
                    .set(ISSUES.SUMMARY, field.getString("summary"))
                    .set(ISSUES.CREATED_DATE_TIME, field.getLong("created")?.toTimestamp())
                    .set(ISSUES.CREATED_DATE, field.getLong("created").toDate())
                    .set(ISSUES.CREATED_WEEK, field.getLong("created").toWeek())
                    .set(ISSUES.UPDATED_DATE_TIME, field.getLong("updated")?.toTimestamp())
                    .set(ISSUES.UPDATED_DATE, field.getLong("updated").toDate())
                    .set(ISSUES.UPDATED_WEEK, field.getLong("updated").toWeek())
                    .set(ISSUES.RESOLVED_DATE_TIME, field.getLong("resolved")?.toTimestamp())
                    .set(ISSUES.RESOLVED_DATE, field.getLong("resolved").toDate())
                    .set(ISSUES.RESOLVED_WEEK, field.getLong("resolved").toWeek())
                    .set(ISSUES.REPORTER_LOGIN, field.getString("reporterName"))
                    .set(ISSUES.COMMENTS_COUNT, field.getInt("commentsCount"))
                    .set(ISSUES.VOTES, field.getInt("votes"))
                    .set(ISSUES.SUBSYSTEM, field.getString("subsystem"))
                    .set(ISSUES.SLA, field.getString("SLA"))
                    .set(ISSUES.SLA_FIRST_RESPONCE_INDEX, field.getString("SLA по первому ответу"))
                    .set(ISSUES.SLA_FIRST_RESPONCE_DATE_TIME, field.getLong("Дата первого ответа")?.toTimestamp())
                    .set(ISSUES.SLA_FIRST_RESPONCE_DATE, field.getLong("Дата первого ответа")?.toDate())
                    .set(ISSUES.SLA_FIRST_RESPONCE_WEEK, field.getLong("Дата первого ответа")?.toWeek())
                    .set(ISSUES.SLA_SOLUTION_INDEX, field.getString("SLA по решению"))
                    .set(ISSUES.SLA_SOLUTION_DATE_TIME, field.getLong("Дата решения")?.toTimestamp())
                    .set(ISSUES.SLA_SOLUTION_DATE, field.getLong("Дата решения")?.toDate())
                    .set(ISSUES.SLA_SOLUTION_WEEK, field.getLong("Дата решения")?.toWeek())
                    .set(ISSUES.PROJECT, field.getString("projectShortName"))
                    .set(ISSUES.ISSUE_TYPE, field.getString("Type"))
                    .set(ISSUES.STATE, field.getString("State"))
                    .set(ISSUES.PRIORITY, field.getString("Priority"))
                    .set(ISSUES.PP_VERSION, field.getString("Версия Prognoz Platform"))
                    .set(ISSUES.QUALITY_EVALUATION, field.getString("Оценка"))
                    .set(ISSUES.ETS, field.getString("Проект (ETS)"))
                    .set(ISSUES.LOADED_DATE, Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay()))
                    .set(ISSUES.PROJECT_SHORT_NAME, field.getString("projectShortName"))
                    .onDuplicateKeyUpdate()
                    .set(ISSUES.ENTITY_ID, entityId)
                    .set(ISSUES.SUMMARY, field.getString("summary"))
                    .set(ISSUES.CREATED_DATE_TIME, field.getLong("created")?.toTimestamp())
                    .set(ISSUES.CREATED_DATE, field.getLong("created").toDate())
                    .set(ISSUES.CREATED_WEEK, field.getLong("created").toWeek())
                    .set(ISSUES.UPDATED_DATE_TIME, field.getLong("updated")?.toTimestamp())
                    .set(ISSUES.UPDATED_DATE, field.getLong("updated").toDate())
                    .set(ISSUES.UPDATED_WEEK, field.getLong("updated").toWeek())
                    .set(ISSUES.RESOLVED_DATE_TIME, field.getLong("resolved")?.toTimestamp())
                    .set(ISSUES.RESOLVED_DATE, field.getLong("resolved").toDate())
                    .set(ISSUES.RESOLVED_WEEK, field.getLong("resolved").toWeek())
                    .set(ISSUES.REPORTER_LOGIN, field.getString("reporterName"))
                    .set(ISSUES.COMMENTS_COUNT, field.getInt("commentsCount"))
                    .set(ISSUES.VOTES, field.getInt("votes"))
                    .set(ISSUES.SUBSYSTEM, field.getString("subsystem"))
                    .set(ISSUES.SLA, field.getString("SLA"))
                    .set(ISSUES.SLA_FIRST_RESPONCE_INDEX, field.getString("SLA по первому ответу"))
                    .set(ISSUES.SLA_FIRST_RESPONCE_DATE_TIME, field.getLong("Дата первого ответа")?.toTimestamp())
                    .set(ISSUES.SLA_FIRST_RESPONCE_DATE, field.getLong("Дата первого ответа")?.toDate())
                    .set(ISSUES.SLA_FIRST_RESPONCE_WEEK, field.getLong("Дата первого ответа")?.toWeek())
                    .set(ISSUES.SLA_SOLUTION_INDEX, field.getString("SLA по решению"))
                    .set(ISSUES.SLA_SOLUTION_DATE_TIME, field.getLong("Дата решения")?.toTimestamp())
                    .set(ISSUES.SLA_SOLUTION_DATE, field.getLong("Дата решения")?.toDate())
                    .set(ISSUES.SLA_SOLUTION_WEEK, field.getLong("Дата решения")?.toWeek())
                    .set(ISSUES.PROJECT, field.getString("projectShortName"))
                    .set(ISSUES.ISSUE_TYPE, field.getString("Type"))
                    .set(ISSUES.STATE, field.getString("State"))
                    .set(ISSUES.PRIORITY, field.getString("Priority"))
                    .set(ISSUES.PP_VERSION, field.getString("Версия Prognoz Platform"))
                    .set(ISSUES.QUALITY_EVALUATION, field.getString("Оценка"))
                    .set(ISSUES.ETS, field.getString("Проект (ETS)"))
                    .set(ISSUES.LOADED_DATE, Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay()))
                    .set(ISSUES.PROJECT_SHORT_NAME, field.getString("projectShortName"))
                    .execute()

        } catch (e: DataAccessException) {
            println(e)
            writeError(this.toString(), e.message ?: "")
        }
    }

    private fun Issue.saveCustomFields() {
        dslContext.deleteFrom(CUSTOM_FIELD_VALUES).where(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(id)).execute()
        field.forEach { field ->
            //TODO извлечь данные из assignee
            val fieldName = field.name.removeSurrounding("[", "]")
            val fieldValue = field.value.toString()
            val value = when (fieldName) {
                "Assignee" -> fieldValue.substringAfter("value=").substringBefore(", fullName=")
                else -> fieldValue.removeSurrounding("[", "]")
            }
            try {
                dslContext
                        .insertInto(CUSTOM_FIELD_VALUES)
                        .set(CUSTOM_FIELD_VALUES.ISSUE_ID, id)
                        .set(CUSTOM_FIELD_VALUES.FIELD_NAME, fieldName)
                        .set(CUSTOM_FIELD_VALUES.FIELD_VALUE, value)
                        .execute()
            } catch (e: DataAccessException) {
                println(e.message)
                writeError(field.toString(), e.message ?: "")
            }
        }
    }

    private fun Issue.getComments() {
        dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.eq(id)).execute()
        comment.forEach { comment ->
            try {
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
            } catch (e: DataAccessException) {
                println(e.message)
                writeError(comment.toString(), e.message ?: "")
            }
        }
    }

    private fun getTimeAccounting(id: String) {
        dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.eq(id)).execute()
        WorkItemRetrofitService.create().getWorkItems(AUTH, id).execute()?.body()?.forEach {
            try {
                /*println(it)*/
                dslContext
                        .insertInto(WORK_ITEMS)
                        .set(WORK_ITEMS.ISSUE_ID, id)
                        .set(WORK_ITEMS.WI_URL, it.url)
                        .set(WORK_ITEMS.WI_ID, it.id)
                        .set(WORK_ITEMS.WI_DATE, it.date.toTimestamp())
                        .set(WORK_ITEMS.WI_CREATED, it.created.toTimestamp())
                        .set(WORK_ITEMS.WI_UPDATED, it.updated?.toTimestamp())
                        .set(WORK_ITEMS.WI_DURATION, it.duration)
                        .set(WORK_ITEMS.AUTHOR_LOGIN, it.author.login)
                        .set(WORK_ITEMS.AUTHOR_RING_ID, it.author.ringId)
                        .set(WORK_ITEMS.AUTHOR_URL, it.author.url)
                        .set(WORK_ITEMS.WORK_NAME, it.worktype?.name)
                        .set(WORK_ITEMS.WORK_TYPE_ID, it.worktype?.id)
                        .set(WORK_ITEMS.WORK_TYPE_AUTO_ATTACHED, it.worktype?.autoAttached)
                        .set(WORK_ITEMS.WORK_TYPE_URL, it.worktype?.url)
                        .set(WORK_ITEMS.DESCRIPTION, it.description)
                        .execute()
            } catch (e: DataAccessException) {
                println(e.message)
                writeError(it.toString(), e.message ?: "")
            }
        }
    }

    private fun getIssueHistory(issueId: String) {
        dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.eq(issueId)).execute()
        HistoryRetrofitService.create().getIssueHistory(AUTH, issueId).execute().body()?.change?.forEach { change ->
            val updated = change.field.filter { it.name == "updated" }[0].value
            change.field
                    .filter { it.name != "updated" && it.name != "updaterName" && it.name != "attachments" }
                    .forEach { field ->
                        val historyItem = IssueHistoryItem(
                                issueId = issueId,
                                author = change.field.filter { it.name == "updaterName" }[0].value.toString(),
                                updateDateTime = updated.toString().toLong().toTimestamp(),
                                fieldName = field.name,
                                value = field.value.toString(),
                                oldValue = field.oldValue.toString(),
                                newValue = field.newValue.toString())

                        try {
                            dslContext
                                    .insertInto(ISSUE_HISTORY)
                                    .set(ISSUE_HISTORY.ISSUE_ID, historyItem.issueId)
                                    .set(ISSUE_HISTORY.AUTHOR, historyItem.author)
                                    .set(ISSUE_HISTORY.UPDATE_DATE_TIME, historyItem.updateDateTime)
                                    .set(ISSUE_HISTORY.FIELD_NAME, historyItem.fieldName)
                                    .set(ISSUE_HISTORY.VALUE_TYPE, "string")
                                    .set(ISSUE_HISTORY.OLD_VALUE_STRING, historyItem.oldValue?.removeSurrounding("[", "]"))
                                    .set(ISSUE_HISTORY.NEW_VALUE_STRING, historyItem.newValue?.removeSurrounding("[", "]"))
                                    .set(ISSUE_HISTORY.UPDATE_WEEK, historyItem.updateDateTime.time.toWeek())
                                    .execute()
                        } catch (e: DataAccessException) {
                            println(e)
                        }
                    }
        }
        val d = dslContext
                .select(DSL.max(ISSUE_HISTORY.UPDATE_DATE_TIME).`as`("nt"))
                .from(ISSUE_HISTORY)
                .where(ISSUE_HISTORY.ISSUE_ID.eq(issueId).and(ISSUE_HISTORY.FIELD_NAME.eq("Оценка")))
                .fetchOneInto(NullableTimestamp::class.java)
        if (d.nt != null)
            dslContext.update(ISSUES)
                    .set(ISSUES.QUALITY_EVALUATION_DATE_TIME, d.nt)
                    .set(ISSUES.QUALITY_EVALUATION_DATE, d.nt.time.toDate())
                    .set(ISSUES.QUALITY_EVALUATION_WEEK, d.nt.time.toWeek())
                    .where(ISSUES.ID.eq(issueId))
                    .execute()
    }

    data class NullableTimestamp(
            val nt: Timestamp?
    )

    fun checkIssues() {
        var count = 0
        val deletedItems = arrayListOf<String>()
        val result = dslContext.select(ISSUES.ID).from(ISSUES).fetchInto(String::class.java)
        val interval = (result.size / 100) + 1
        result.forEachIndexed { index, issueId ->
            val requestResult = IssueExistenceRetrofitService.create().check(AUTH, issueId).execute()
            val currentProject = requestResult.body()?.field?.getString("projectShortName")
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
        dslContext.deleteFrom(ISSUES).where(ISSUES.ID.`in`(issues)).execute()
        dslContext.deleteFrom(CUSTOM_FIELD_VALUES).where((CUSTOM_FIELD_VALUES.ISSUE_ID.`in`(issues))).execute()
        dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.`in`(issues)).execute()
        dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.`in`(issues)).execute()
        dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.`in`(issues)).execute()
        return 0
    }

    private fun writeError(item: String, message: String) {
        dslContext
                .insertInto(ERROR_LOG)
                .set(ERROR_LOG.DATE, Timestamp.valueOf(LocalDateTime.now()))
                .set(ERROR_LOG.ITEM, item)
                .set(ERROR_LOG.ERROR, message)
                .execute()
    }
}