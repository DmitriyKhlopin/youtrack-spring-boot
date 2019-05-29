package fsight.youtrack.api.issues

import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
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
        var type: String? = null,
        var comment: String? = null,
        var issue: String? = null,
        var tfsIssues: ArrayList<IssueTFSData> = arrayListOf(),
        var tfsData: ArrayList<TFSPlainIssue> = arrayListOf(),
        var timeUser: Long? = null,
        var timeAgent: Long? = null,
        var timeDeveloper: Long? = null
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

    override fun getHighPriorityIssuesWithTFSDetails(
        projectsString: String?,
        customersString: String?,
        prioritiesString: String?,
        statesString: String?
    ): Any {
        val projectsFilter = projectsString?.removeSurrounding("[", "]")?.split(",").orEmpty()
        val customersFilter = customersString?.removeSurrounding("[", "]")?.split(",").orEmpty()
        val prioritiesFilter = prioritiesString?.removeSurrounding("[", "]")?.split(",").orEmpty()
        val statesFilter = statesString?.removeSurrounding("[", "]")?.split(",").orEmpty()

        val issues = ISSUES.`as`("i")
        val priority = CUSTOM_FIELD_VALUES.`as`("priority")
        val customer = CUSTOM_FIELD_VALUES.`as`("customer")
        val state = CUSTOM_FIELD_VALUES.`as`("state")
        val issue = CUSTOM_FIELD_VALUES.`as`("issue")
        val type = CUSTOM_FIELD_VALUES.`as`("type")
        val comment: Field<Int> = dslContext.select(WORK_ITEMS.DESCRIPTION)
            .from(WORK_ITEMS)
            .where(WORK_ITEMS.ISSUE_ID.eq(issues.ID))
            .and(WORK_ITEMS.WORK_NAME.eq("Анализ сроков выполнения"))
            .orderBy(WORK_ITEMS.WI_CREATED.desc())
            .limit(1)
            .asField()
        println(statesFilter)
        val statesCondition =
            when {
                (statesFilter.contains("Активные") && statesFilter.contains("Завершенные")) || statesFilter.isEmpty() -> {
                    DSL.trueCondition()
                }
                statesFilter.contains("Активные") -> DSL.and(issues.RESOLVED_DATE.isNull)
                statesFilter.contains("Завершенные") -> DSL.and(issues.RESOLVED_DATE.isNotNull)
                else -> DSL.and(issues.RESOLVED_DATE.isNull)
            }


        val query = dslContext
            .select(
                issues.ID.`as`("id"),
                issues.SUMMARY.`as`("summary"),
                issues.CREATED_DATE_TIME.`as`("created"),
                issue.FIELD_VALUE.`as`("issue"),
                state.FIELD_VALUE.`as`("state"),
                comment.`as`("comment"),
                priority.FIELD_VALUE.`as`("priority"),
                issues.ISSUE_TYPE.`as`("type"),
                issues.TIME_USER.`as`("timeUser"),
                issues.TIME_AGENT.`as`("timeAgent"),
                issues.TIME_DEVELOPER.`as`("timeDeveloper")
            )
            .from(issues)
            .leftJoin(priority).on(issues.ID.eq(priority.ISSUE_ID)).and(priority.FIELD_NAME.eq("Priority"))
            .leftJoin(customer).on(issues.ID.eq(customer.ISSUE_ID)).and(customer.FIELD_NAME.eq("Заказчик"))
            .leftJoin(issue).on(issues.ID.eq(issue.ISSUE_ID)).and(issue.FIELD_NAME.eq("Issue"))
            .leftJoin(type).on(issues.ID.eq(type.ISSUE_ID)).and(type.FIELD_NAME.eq("Type"))
            .leftJoin(state).on(issues.ID.eq(state.ISSUE_ID)).and(state.FIELD_NAME.eq("State"))
            .where(issues.PROJECT_SHORT_NAME.`in`(projectsFilter))
            .and(customer.FIELD_VALUE.`in`(customersFilter))
            .and(priority.FIELD_VALUE.`in`(prioritiesFilter))
            .and(statesCondition)
        /*.and(issues.RESOLVED_DATE.isNull)*/
        println(query.sql)
        val result = query.fetchInto(HighPriorityIssue::class.java)
        result.forEachIndexed { index, item ->
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
                        result[index].tfsIssues.add(i)
                    }
                }
            }
            result[index].tfsData = result[index].tfsIssues.transformToIssues()
            result[index].tfsIssues = arrayListOf()
            result[index].timeAgent = result[index].timeAgent?.div(3600)
            result[index].timeUser = result[index].timeUser?.div(3600)
            result[index].timeDeveloper = result[index].timeDeveloper?.div(3600)

        }
        return result
    }
}
