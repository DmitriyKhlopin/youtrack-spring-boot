package fsight.youtrack.api.issues

import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.common.IResolver
import fsight.youtrack.db.IDevOpsProvider
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.etl.timeline.ITimeline
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.IssueTags.ISSUE_TAGS
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.models.toReadableItem
import fsight.youtrack.splitToList
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class Issues(private val dslContext: DSLContext, @Qualifier("tfsDataSource") private val ms: Database) : IIssues {
    @Autowired
    private lateinit var dictionariesService: IDictionary

    @Autowired
    private lateinit var pg: IPGProvider

    @Autowired
    private lateinit var devops: IDevOpsProvider

    @Autowired
    private lateinit var resolver: IResolver

    @Autowired
    private lateinit var timelineService: ITimeline

    private val issues = ISSUES.`as`("i")
    private val priority = CUSTOM_FIELD_VALUES.`as`("priority")
    private val customer = CUSTOM_FIELD_VALUES.`as`("customer")
    private val responsible = CUSTOM_FIELD_VALUES.`as`("responsible")
    private val state = CUSTOM_FIELD_VALUES.`as`("state")
    private val issue = CUSTOM_FIELD_VALUES.`as`("issue")
    private val requirement = CUSTOM_FIELD_VALUES.`as`("requirement")
    private val type = CUSTOM_FIELD_VALUES.`as`("type")
    private val tags = ISSUE_TAGS.`as`("tags")
    private val tagsFilter = ISSUE_TAGS.`as`("tags_filter")
    private val comment: Field<Int> = dslContext.select(WORK_ITEMS.DESCRIPTION)
        .from(WORK_ITEMS)
        .where(WORK_ITEMS.ISSUE_ID.eq(issues.ID))
        .and(WORK_ITEMS.WORK_NAME.eq("Анализ сроков выполнения"))
        .orderBy(WORK_ITEMS.WI_CREATED.desc())
        .limit(1)
        .asField()

    private val commentAuthor: Field<Int> = dslContext.select(WORK_ITEMS.AUTHOR_LOGIN)
        .from(WORK_ITEMS)
        .where(WORK_ITEMS.ISSUE_ID.eq(issues.ID))
        .and(WORK_ITEMS.WORK_NAME.eq("Анализ сроков выполнения"))
        .orderBy(WORK_ITEMS.WI_CREATED.desc())
        .limit(1)
        .asField()


    data class TFSPlainIssue(
        var issueId: Int? = null,
        var issueState: String? = null,
        var issueMergedIn: Int? = null,
        var issueReason: String? = null,
        var issueLastUpdate: String? = null,
        var iterationPath: String? = null,
        var defects: ArrayList<TFSPlainDefect> = arrayListOf()
    )

    data class TFSPlainDefect(
        var defectId: Int? = null,
        var defectState: String? = null,
        var defectReason: String? = null,
        var iterationPath: String? = null,
        var developmentManager: String? = null,
        var defectDeadline: String? = null,
        var changeRequests: ArrayList<TFSPlainChangeRequest> = arrayListOf()
    )

    data class TFSPlainChangeRequest(
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
                defectState = it.defectState,
                defectReason = it.defectReason,
                iterationPath = it.defectIterationPath,
                developmentManager = it.defectDevelopmentManager,
                defectDeadline = it.defectDeadline,
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

    override fun getHighPriorityIssuesWithDevOpsDetails(
        projectsString: String?,
        customersString: String?,
        prioritiesString: String?,
        statesString: String?
    ): Any {
        val projectsFilter = projectsString?.splitToList().orEmpty()
        val customersFilter = customersString?.splitToList().orEmpty()
        val prioritiesFilter = prioritiesString?.splitToList().orEmpty()
        val statesFilter = statesString?.splitToList().orEmpty()

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
                issues.PROJECT_SHORT_NAME.`as`("project"),
                customer.FIELD_VALUE.`as`("customer"),
                issues.SUMMARY.`as`("summary"),
                issues.CREATED_DATE_TIME.`as`("created"),
                issue.FIELD_VALUE.`as`("issue"),
                state.FIELD_VALUE.`as`("state"),
                comment.`as`("comment"),
                commentAuthor.`as`("commentAuthor"),
                priority.FIELD_VALUE.`as`("priority"),
                issues.ISSUE_TYPE.`as`("type"),
                responsible.FIELD_VALUE.`as`("assignee"),
                issues.TIME_USER.`as`("timeUser"),
                issues.TIME_AGENT.`as`("timeAgent"),
                issues.TIME_DEVELOPER.`as`("timeDeveloper")
            )
            .from(issues)
            .leftJoin(priority).on(issues.ID.eq(priority.ISSUE_ID)).and(priority.FIELD_NAME.eq("Priority"))
            .leftJoin(customer).on(issues.ID.eq(customer.ISSUE_ID)).and(customer.FIELD_NAME.eq("Заказчик"))
            .leftJoin(responsible).on(issues.ID.eq(responsible.ISSUE_ID)).and(responsible.FIELD_NAME.eq("Assignee"))
            .leftJoin(issue).on(issues.ID.eq(issue.ISSUE_ID)).and(issue.FIELD_NAME.eq("Issue"))
            .leftJoin(type).on(issues.ID.eq(type.ISSUE_ID)).and(type.FIELD_NAME.eq("Type"))
            .leftJoin(state).on(issues.ID.eq(state.ISSUE_ID)).and(state.FIELD_NAME.eq("State"))
            .where(issues.PROJECT_SHORT_NAME.`in`(projectsFilter))
            .and(customer.FIELD_VALUE.`in`(customersFilter))
            .and(priority.FIELD_VALUE.`in`(prioritiesFilter))
            .and(statesCondition)
        val result = query.fetchInto(IssueWiThDetails::class.java)
        result.forEachIndexed { index, item ->
            val issueIds = item.issue?.split(",")?.joinToString("','", prefix = "'", postfix = "'")
            if (issueIds != null && issueIds != "\'null\'") {
                val statement = """
            SELECT issue.system_id                                  AS issue_id,
       issue.System_State                               AS issue_state,
       issue.Prognoz_P7_ChangeRequest_MergedIn          AS issue_merged_in,
       issue.System_Reason                              AS issue_reason,
       issue.System_ChangedDate                         AS issue_last_update,
       issue.IterationPath                              AS issue_iteration_path,
       linked_to_issue.System_WorkItemType              AS linked_to_issue,
       defect.System_Id                                 AS defect_id,
       defect.System_State                              AS defect_state,
       defect.IterationPath                             AS defect_iteration_path,
       defect.System_Reason                             AS defect_reason,
       defect.Prognoz_VSTS_Common_DevelopmentManager    AS defect_development_manager,
       defect.System_ChangedDate                        AS defect_last_update,
       defect.Prognoz_VSTS_Common_Deadline              AS defect_deadline,
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
WHERE issue.System_Id IN ($issueIds)
  AND issue.System_WorkItemType = 'Bug'
  AND issue.TeamProjectCollectionSK = 37
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
                                defectState = rs.getString("defect_state"),
                                defectReason = rs.getString("defect_reason"),
                                defectIterationPath = rs.getString("issue_iteration_path"),
                                defectDevelopmentManager = rs.getString("defect_development_manager"),
                                defectDeadline = rs.getString("defect_deadline"),
                                changeRequestId = rs.getString("change_request_id")?.toInt(),
                                changeRequestMergedIn = rs.getString("changed_request_merged_in"),
                                iterationPath = rs.getString("iteration_path"),
                                changeRequestReason = rs.getString("change_request_reason")
                            )
                            result[index].tfsPlainIssues.add(i)
                        }
                    }
                }
                result[index].tfsData = result[index].tfsPlainIssues.transformToIssues()
                result[index].timeAgent = result[index].timeAgent?.div(3600)
                result[index].timeUser = result[index].timeUser?.div(3600)
                result[index].timeDeveloper = result[index].timeDeveloper?.div(3600)
            }
        }
        return result
    }

    fun getProjectsCondition(issueFilter: IssueFilter): Condition =
        if (issueFilter.projects.isEmpty()) DSL.and(issues.PROJECT_SHORT_NAME.`in`(dictionariesService.commercialProjects.map { it.value })) else DSL.and(issues.PROJECT_SHORT_NAME.`in`(issueFilter.projects))

    fun getPrioritiesCondition(issueFilter: IssueFilter): Condition = if (issueFilter.priorities.isEmpty()) DSL.and(DSL.trueCondition()) else DSL.and(issues.PRIORITY.`in`(issueFilter.priorities))
    fun getStatesCondition(issueFilter: IssueFilter): Condition = if (issueFilter.states.isEmpty()) DSL.and(DSL.trueCondition()) else DSL.and(issues.STATE.`in`(issueFilter.states))
    fun getCustomersCondition(issueFilter: IssueFilter): Condition = if (issueFilter.customers.isEmpty()) DSL.and(DSL.trueCondition()) else DSL.and(issues.CUSTOMER.`in`(issueFilter.customers))
    fun getTagsCondition(issueFilter: IssueFilter): Condition =
        when {
            issueFilter.tags.isEmpty() -> DSL.and(DSL.trueCondition())
            !issueFilter.allTags -> DSL.and(issues.ID.eq(DSL.any(DSL.select(tagsFilter.ISSUE_ID).from(tagsFilter).where(tagsFilter.TAG.`in`(issueFilter.tags)))))
            /*issueFilter.allTags -> DSL.and(issues.ID.eq(DSL.all(DSL.select(tagsFilter.ISSUE_ID).from(tagsFilter).where(tagsFilter.TAG.`in`(issueFilter.tags)))))*/
            issueFilter.allTags -> DSL.and(DSL.`val`(issueFilter.tags.size).eq(DSL.selectCount().from(tagsFilter).where(tagsFilter.TAG.`in`(issueFilter.tags)).and(tagsFilter.ISSUE_ID.eq(issues.ID))))
            else -> DSL.and(DSL.trueCondition())
        }


    override fun getIssuesWithTFSDetails(issueFilter: IssueFilter): Any {
        val issues = pg.getYouTrackIssuesWithDetails(issueFilter)
        /*val bugsDetails = devops.getDevOpsItemsByIdsAndType(issues.getBugs(), "Bug")
        val requirementsDetails = devops.getDevOpsItemsByIdsAndType(issues.getRequirements(), "Feature")*/
        val wiDetails = devops.getDevOpsItemsByIds(issues.getBugsAndFeatures())
        val columns = listOf("Id", "FieldId", "IntValue", "FloatValue", "DateTimeValue", "StringValue")
        val priorityReasons = devops.getCustomFieldsValues(issues.getBugsAndFeatures(), listOf(10346), columns)
        wiDetails.forEach { b ->
            b.team = resolver.resolveAreaToTeam(b.area ?: "")
            b.teamPo = resolver.resolveAreaToPo(b.area ?: "")
            b.priorityReason = priorityReasons.firstOrNull { e -> e.id == b.systemId }?.string
        }
        /** Merging issues with bugs */
        issues.forEach { j ->
            j.devOpsBugs.addAll(j.getBugs().mapNotNull { b -> wiDetails.firstOrNull { e -> e.systemId == b } })
            j.devOpsRequirements.addAll(j.getRequirements().mapNotNull { b -> wiDetails.firstOrNull { e -> e.systemId == b } })
            j.plainTags?.removeSurrounding("{", "}")?.split(",")?.let { j.tags.addAll(it) }
        }
        return issues
    }


    override fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any {
        return pg.getIssuesBySigmaValue(days, issueFilter)
    }

    override fun getDetailedStateTransitions(issueId: String): Any {
        val i = pg.getIssuesDetailedTimeline(issueId)
        i.forEach {
            it.timeSpent = timelineService.calculatePeriod(it).toLong()
        }
        return i.sortedBy { it.order }/*.map { it.toReadableItem() }*/
    }
}

