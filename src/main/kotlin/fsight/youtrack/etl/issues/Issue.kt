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
    override fun getIssues(customFilter: String?): Int {
        val issueIds = arrayListOf<String?>()
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
        val top = 40
        var skip = 0
        var stored = 0
        val filter = customFilter ?: getFilter()
        println("Actual filter: $filter")

        do {
            val request = if (filter == null)
                YouTrackAPI.create(Converter.GSON)
                    .getIssueList(auth = AUTH, fields = fields, top = top, skip = skip)
            else
                YouTrackAPI.create(Converter.GSON)
                    .getIssueList(auth = AUTH, fields = fields, top = top, skip = skip, query = filter)

            val result = request.execute().body()
            issueIds.addAll(result?.mapNotNull { it.idReadable } ?: listOf())
            skip += result?.size ?: 0
            stored += result?.map { it.toIssueRecord() }?.loadToDatabase(dslContext) ?: 0

            result?.forEach {
                it.saveComments()
                it.saveCustomFields()
            }
            print("Loaded ${issueIds.size} issues, stored $stored\r")
        } while (result?.size ?: 0 > 0)

        //TODO оптимизировать импорт истории для проблемных запросов
        issueIds.filterNot { it in listOf("SIGMA-17") }.filterNotNull().forEach {
            getIssueHistory(it)
            getTimeAccounting(it)
        }
        println("Loaded $skip issues")
        importLogService.saveLog(
            ImportLogModel(
                timestamp = Timestamp(System.currentTimeMillis()),
                source = "issues (filtered by '$customFilter')",
                table = "issues",
                items = skip
            )
        )
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

    //TODO преобразовать в loadInto
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

    //TODO преобразовать в loadInto
    private fun YouTrackIssue.saveComments() {
        dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.eq(idReadable)).execute()
        //TODO аменить на loadInto
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

    private fun getTimeAccounting(idReadable: String) {
        dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.eq(idReadable)).execute()
        //TODO заменить на loadInto
        YouTrackAPI.create(Converter.GSON).getWorkItems(AUTH, idReadable).execute().body()?.forEach {
            dslContext
                .insertInto(WORK_ITEMS)
                .set(WORK_ITEMS.ISSUE_ID, idReadable)
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

    override fun getIssueHistory(idReadable: String) {
        val issueActivities =
            YouTrackAPI.create(Converter.GSON).getCustomFieldsHistory(auth = AUTH, issueId = idReadable).execute()
        val items = issueActivities.body()?.activities?.map { it.toIssueHistoryRecord(idReadable) }
        dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.eq(idReadable)).execute()
        val stored = dslContext.loadInto(ISSUE_HISTORY).loadRecords(items).fields(
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
        ).execute().stored()
        println("$idReadable: stored $stored history items")
    }

    override fun findDeletedIssues() {
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
            if (index % interval == 0) print("Checked ${index * 100 / result.size}% of issues\r")
        }
        println()
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
            ETL.etlState = ETLState.DONE
        }
    }
}
