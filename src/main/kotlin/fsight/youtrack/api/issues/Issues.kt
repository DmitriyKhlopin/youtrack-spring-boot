package fsight.youtrack.api.issues

import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooq.DSLContext
import org.jooq.Field
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class Issues(private val dslContext: DSLContext, @Qualifier("tfsDataSource") private val ms: Database) : IIssues {
    internal class HighPriorityIssue(
        var id: String? = null,
        var summary: String? = null,
        var created: Timestamp? = null,
        var priority: String? = null,
        var state: String? = null,
        var comment: String? = null,
        var issue: String? = null,
        var tfsIssues: ArrayList<IssueTFSData> = arrayListOf(),
        var tfsData: ArrayList<TFSPlainIssue> = arrayListOf()
    )


    internal class IssueTFSData(
        var issueId: Int? = null,
        var issueState: String? = null,
        var issueMergedIn: Int? = null,
        var issueReason: String? = null,
        var issueLastUpdate: String? = null,
        var issueIterationPath: String? = null,
        var defectId: Int? = null,
        var defectReason: String? = null,
        var defectIterationPath: String? = null,
        var defectDevelopmentManager: String? = null,
        var changeRequestId: Int? = null,
        var changeRequestMergedIn: String? = null,
        var iterationPath: String? = null,
        var changeRequestReason: String? = null
    )

    internal class TFSPlainIssue(
        var issueId: Int? = null,
        var issueState: String? = null,
        var issueMergedIn: Int? = null,
        var issueReason: String? = null,
        var issueLastUpdate: String? = null,
        var iterationPath: String? = null,
        var defects: ArrayList<TFSPlainDefect> = arrayListOf()
    )

    internal class TFSPlainDefect(
        var defectId: Int? = null,
        var defectReason: String? = null,
        var iterationPath: String? = null,
        var developmentManager: String? = null,
        var changeRequests: ArrayList<TFSPlainChangeRequest> = arrayListOf()
    )

    internal class TFSPlainChangeRequest(
        var changeRequestId: Int? = null,
        var changeRequestMergedIn: String? = null,
        var iterationPath: String? = null,
        var changeRequestReason: String? = null
    )

    internal fun ArrayList<IssueTFSData>.transformToIssues(): ArrayList<TFSPlainIssue> =
        ArrayList(this.map {
            TFSPlainIssue(
                issueId = it.issueId,
                issueState = it.issueState,
                issueMergedIn = it.issueMergedIn,
                issueReason = it.issueReason,
                issueLastUpdate = it.issueLastUpdate,
                iterationPath = it.issueIterationPath,
                defects = this.transformToDefects(it.issueId)
            )
        }.distinctBy { it.issueId })

    internal fun ArrayList<IssueTFSData>.transformToDefects(issueId: Int?): ArrayList<TFSPlainDefect> =
        ArrayList(this.filter { defect -> defect.defectId != null && defect.issueId == issueId }.map {
            TFSPlainDefect(
                defectId = it.defectId,
                defectReason = it.defectReason,
                iterationPath = it.defectIterationPath,
                developmentManager = it.defectDevelopmentManager,
                changeRequests = this.transformToChangeRequests(it.defectId)
            )
        }.distinctBy { it.defectId })

    internal fun ArrayList<IssueTFSData>.transformToChangeRequests(defectId: Int?): ArrayList<TFSPlainChangeRequest> =
        ArrayList(this.filter { changeRequest -> changeRequest.changeRequestId != null && changeRequest.defectId == defectId }.map {
            TFSPlainChangeRequest(
                changeRequestId = it.changeRequestId,
                changeRequestMergedIn = it.changeRequestMergedIn,
                iterationPath = it.iterationPath,
                changeRequestReason = it.changeRequestReason
            )
        }.distinctBy { it.changeRequestId })


    /**
    SELECT issues.id,
    issues.summary,
    issues.created_date_time,
    priority.field_value  AS issue_priority,
    type.field_value      AS issue_type,
    (SELECT work_items.description
    FROM work_items
    WHERE issues.id = work_items.issue_id
    AND work_items.work_name = 'Анализ качества выполнения'
    ORDER BY work_items.wi_created DESC
    LIMIT 1)             AS issue_comment,
    tfs_issue.field_value AS tfs_issue
    FROM issues
    LEFT JOIN custom_field_values priority ON issues.id = priority.issue_id AND priority.field_name = 'Priority'
    LEFT JOIN custom_field_values tfs_issue ON issues.id = tfs_issue.issue_id AND tfs_issue.field_name = 'Issue'
    LEFT JOIN custom_field_values type ON issues.id = type.issue_id AND type.field_name = 'Type'
    WHERE resolved_date IS NULL
    AND issues.project_short_name NOT IN ('SD', 'PO', 'TC', 'W', 'PP_Lic')
    AND priority.field_value = 'Major'
    AND type.field_value != 'Feature'
     */
    override fun getHighPriorityIssuesWithTFSDetails(
        projectsString: String?,
        customersString: String?,
        prioritiesString: String?
    ): Any {
        val filter = projectsString?.removeSurrounding("[", "]")?.split(",")?.map { "\'$it\'" }?.joinToString(
            ",",
            prefix = "(",
            postfix = ")"
        ) ?: "()"
        println(filter)
        val f = projectsString?.removeSurrounding("[", "]")?.split(",").orEmpty()
        val priority = CUSTOM_FIELD_VALUES.`as`("priority")
        val state = CUSTOM_FIELD_VALUES.`as`("state")
        val issue = CUSTOM_FIELD_VALUES.`as`("issue")
        val type = CUSTOM_FIELD_VALUES.`as`("type")
        val comment: Field<Int> = dslContext.select(WORK_ITEMS.DESCRIPTION)
            .from(WORK_ITEMS)
            .where(WORK_ITEMS.ISSUE_ID.eq(ISSUES.ID))
            .and(WORK_ITEMS.WORK_NAME.eq("Анализ сроков выполнения"))
            .orderBy(WORK_ITEMS.WI_CREATED.desc())
            .limit(1)
            .asField()

        val r = dslContext
            .select(
                ISSUES.ID.`as`("id"),
                ISSUES.SUMMARY.`as`("summary"),
                ISSUES.CREATED_DATE_TIME.`as`("created"),
                issue.FIELD_VALUE.`as`("issue"),
                state.FIELD_VALUE.`as`("state"),
                comment.`as`("comment"),
                priority.FIELD_VALUE.`as`("priority")
            )
            .from(ISSUES)
            .leftJoin(priority).on(ISSUES.ID.eq(priority.ISSUE_ID)).and(priority.FIELD_NAME.eq("Priority"))
            .leftJoin(issue).on(ISSUES.ID.eq(issue.ISSUE_ID)).and(issue.FIELD_NAME.eq("Issue"))
            .leftJoin(type).on(ISSUES.ID.eq(type.ISSUE_ID)).and(type.FIELD_NAME.eq("Type"))
            .leftJoin(state).on(ISSUES.ID.eq(state.ISSUE_ID)).and(state.FIELD_NAME.eq("State"))
            .where(ISSUES.RESOLVED_DATE.isNull)
            //.and(priority.FIELD_VALUE.eq("Major"))
            //.and(type.FIELD_VALUE.notEqual("Feature"))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(f))
            //.and(ISSUES.PROJECT_SHORT_NAME.notIn(listOf("SD", "PO", "TC", "W", "PP_Lic")))
            .fetchInto(HighPriorityIssue::class.java)

        r.forEachIndexed { index, item ->
            val statement = """
            SELECT issue.system_id                                  AS issue_id,
       issue.System_State                               AS issue_state,
       issue.Prognoz_P7_ChangeRequest_MergedIn          AS issue_merged_in,
       issue.System_Reason                              AS issue_reason,
       issue.System_ChangedDate                         AS issue_last_update,
       issue.IterationPath                              AS issue_iteration_path,
       linked_to_issue.System_WorkItemType              AS linked_to_issue,
       defect.System_Id                                 AS defect_id,
       defect.IterationPath                             AS defect_iteration_path,
       defect.System_Reason                             AS defect_reason,
       defect.Prognoz_VSTS_Common_DevelopmentManager    AS defect_development_manager,
       defect.System_ChangedDate                        AS defect_last_update,
       linked_to_defect.System_WorkItemType             AS linked_to_defect,
       change_request.System_Id                         AS change_request_id,
       change_request.Prognoz_P7_ChangeRequest_MergedIn AS changed_request_merged_in,
       change_request.IterationPath                     AS iteration_path,
       change_request.System_Reason                     AS change_request_reason
FROM CurrentWorkItemView issue
         LEFT JOIN vFactLinkedCurrentWorkItem link_to_defect ON issue.WorkItemSK = link_to_defect.SourceWorkItemSK
         LEFT JOIN CurrentWorkItemView linked_to_issue ON link_to_defect.TargetWorkitemSK = linked_to_issue.WorkItemSK
         LEFT JOIN CurrentWorkItemView defect
                   ON link_to_defect.TargetWorkitemSK = defect.WorkItemSK AND defect.System_WorkItemType = 'Defect'
         LEFT JOIN vFactLinkedCurrentWorkItem link_to_change_request
                   ON defect.WorkItemSK = link_to_change_request.SourceWorkItemSK
         LEFT JOIN CurrentWorkItemView linked_to_defect
                   ON link_to_change_request.TargetWorkitemSK = linked_to_defect.WorkItemSK
         LEFT JOIN CurrentWorkItemView change_request
                   ON link_to_change_request.TargetWorkitemSK = change_request.WorkItemSK AND
                      change_request.System_WorkItemType = 'Change request'
WHERE issue.System_Id IN (${item.issue?.split(",")?.joinToString("','", prefix = "'", postfix = "'")})
  AND issue.System_WorkItemType = 'Issue'
  AND (linked_to_issue.System_WorkItemType NOT IN ('Issue', 'Task') OR linked_to_issue.System_WorkItemType IS NULL)
      """

            transaction(ms) {
                TransactionManager.current().exec(statement) { rs ->
                    while (rs.next()) {
                        val i = IssueTFSData(
                            issueId = rs.getString("issue_id").toInt(),
                            issueState = rs.getString("issue_state"),
                            issueMergedIn = rs.getString("issue_merged_in")?.toInt(),
                            issueReason = rs.getString("issue_reason"),
                            issueLastUpdate = rs.getString("issue_last_update"),
                            issueIterationPath = rs.getString("issue_iteration_path"),
                            defectId = rs.getString("defect_id")?.toInt(),
                            defectReason = rs.getString("defect_reason"),
                            defectIterationPath = rs.getString("issue_iteration_path"),
                            defectDevelopmentManager = rs.getString("defect_development_manager"),
                            changeRequestId = rs.getString("change_request_id")?.toInt(),
                            changeRequestMergedIn = rs.getString("changed_request_merged_in"),
                            iterationPath = rs.getString("iteration_path"),
                            changeRequestReason = rs.getString("change_request_reason")
                        )
                        r[index].tfsIssues.add(i)
                    }
                }
            }
            r[index].tfsData = r[index].tfsIssues.transformToIssues()
            r[index].tfsIssues = arrayListOf()
        }
        return r
    }
}
