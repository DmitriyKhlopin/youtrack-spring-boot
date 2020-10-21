package fsight.youtrack.db

import com.google.gson.Gson
import fsight.youtrack.api.issues.IssueWiThDetails
import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Dynamics.DYNAMICS
import fsight.youtrack.generated.jooq.tables.EtsNames.ETS_NAMES
import fsight.youtrack.generated.jooq.tables.Hooks.HOOKS
import fsight.youtrack.generated.jooq.tables.IssueTags.ISSUE_TAGS
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.PartnerCustomers.PARTNER_CUSTOMERS
import fsight.youtrack.generated.jooq.tables.ProjectType.PROJECT_TYPE
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldValuesRecord
import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.splitToList
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class PGProvider(private val dsl: DSLContext) : IPGProvider {
    private val issuesTable = ISSUES.`as`("issuesTable")
    private val projectTypesTable = PROJECT_TYPE.`as`("projectTypeTable")
    private val prioritiesTable = CUSTOM_FIELD_VALUES.`as`("priority")
    private val statesTable = CUSTOM_FIELD_VALUES.`as`("statesTable")
    private val detailedStatesTable = CUSTOM_FIELD_VALUES.`as`("detailedStatesTable")
    private val typesTable = CUSTOM_FIELD_VALUES.`as`("typesTable")
    private val teamsTable = CUSTOM_FIELD_VALUES.`as`("teamsTable")
    private val bugsTable = CUSTOM_FIELD_VALUES.`as`("issue")
    private val customersTable = CUSTOM_FIELD_VALUES.`as`("customer")
    private val assigneesTable = CUSTOM_FIELD_VALUES.`as`("responsible")
    private val requirementsTable = CUSTOM_FIELD_VALUES.`as`("requirement")
    private val tagsTable = ISSUE_TAGS.`as`("tags")
    private val stateCommentsTable = dsl.select(WORK_ITEMS.DESCRIPTION, WORK_ITEMS.AUTHOR_LOGIN, WORK_ITEMS.ISSUE_ID)
        .from(WORK_ITEMS)
        .where(WORK_ITEMS.WORK_NAME.eq("Анализ сроков выполнения"))
        .orderBy(WORK_ITEMS.WI_CREATED.desc())
        .limit(1)
        .asTable()

    private fun IssueFilter.toProjectsCondition() = if (this.projects.isEmpty()) DSL.trueCondition() else DSL.and(issuesTable.PROJECT_SHORT_NAME.`in`(this.projects))
    private fun IssueFilter.toCustomersCondition() = if (this.customers.isEmpty()) DSL.trueCondition() else DSL.and(customersTable.FIELD_VALUE.`in`(this.customers))
    private fun IssueFilter.toPrioritiesCondition() = if (this.priorities.isEmpty()) DSL.trueCondition() else DSL.and(prioritiesTable.FIELD_VALUE.`in`(this.priorities))
    private fun IssueFilter.toTypesCondition() = if (this.types.isEmpty()) DSL.trueCondition() else DSL.and(typesTable.FIELD_VALUE.`in`(this.types))
    private fun IssueFilter.toStatesCondition() = if (this.states.isEmpty()) DSL.trueCondition() else DSL.and(statesTable.FIELD_VALUE.`in`(this.states))
    private fun IssueFilter.toTagsCondition() = if (this.tags.isEmpty()) DSL.trueCondition() else DSL.and(DSL.`val`(this.tags.size).eq(DSL.selectCount().from(tagsTable).where(tagsTable.TAG.`in`(this.tags)).and(tagsTable.ISSUE_ID.eq(issuesTable.ID))))


    override fun saveHookToDatabase(
        body: Hook?,
        fieldState: String?,
        fieldDetailedState: String?,
        errorMessage: String?,
        inferredState: String?,
        commands: ArrayList<String>?,
        type: String,
        rule: ArrayList<Pair<String, Int>>?
    ): Timestamp {
        return dsl
            .insertInto(HOOKS)
            .set(HOOKS.RECORD_DATE_TIME, Timestamp.from(Instant.now()))
            .set(HOOKS.HOOK_BODY, Gson().toJson(body).toString())
            .set(HOOKS.FIELD_STATE, fieldState)
            .set(HOOKS.FIELD_DETAILED_STATE, fieldDetailedState)
            .set(HOOKS.ERROR_MESSAGE, errorMessage)
            .set(HOOKS.INFERRED_STATE, inferredState)
            .set(HOOKS.COMMANDS, commands?.joinToString(separator = " "))
            .set(HOOKS.TYPE, type)
            .returning(HOOKS.RECORD_DATE_TIME)
            .fetchOne().recordDateTime
    }

    override fun getDevOpsAssignees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            ETS_NAMES.ETS_NAME.`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")
        )
            .from(ETS_NAMES)
            .fetchInto(ETSNameRecord::class.java)
    }

    override fun getSupportEmployees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            ETS_NAMES.ETS_NAME.`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")
        )
            .from(ETS_NAMES)
            .where(ETS_NAMES.SUPPORT)
            .fetchInto(ETSNameRecord::class.java)
    }

    override fun getIssueIdsByWIId(id: Int): List<String> {
        return dsl
            .select(CUSTOM_FIELD_VALUES.ISSUE_ID, CUSTOM_FIELD_VALUES.FIELD_VALUE)
            .from(CUSTOM_FIELD_VALUES)
            .where(
                CUSTOM_FIELD_VALUES.FIELD_NAME.`in`(listOf("Issue", "Requirement"))
                    .and(CUSTOM_FIELD_VALUES.FIELD_VALUE.like("%%$id%%"))
            )
            .fetchInto(CustomFieldValuesRecord::class.java)
            .filter { it.fieldValue.replace(" ", "").splitToList().contains(id.toString()) }
            .map { it.issueId }
    }

    override fun getIssuesIdsInDevelopment(): List<String> {
        return dsl
            .select(CUSTOM_FIELD_VALUES.ISSUE_ID)
            .from(CUSTOM_FIELD_VALUES)
            .leftJoin(teamsTable).on(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(teamsTable.ISSUE_ID).and(teamsTable.FIELD_VALUE.eq("Команда")))
            .where(CUSTOM_FIELD_VALUES.FIELD_NAME.`in`(listOf("State")).and(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq("Направлена разработчику")))
            .and(teamsTable.FIELD_VALUE.isNull)
            /*.limit(10)*/
            .fetchInto(CustomFieldValuesRecord::class.java)
            .map { it.issueId }
    }

    override fun getDynamicsData(projects: String, dateFrom: Timestamp, dateTo: Timestamp): List<Dynamics> {
        return dsl.select(
            DYNAMICS.W.`as`("week"),
            DSL.sum(DYNAMICS.ACTIVE).`as`("active"),
            DSL.sum(DYNAMICS.CREATED).`as`("created"),
            DSL.sum(DYNAMICS.RESOLVED).`as`("resolved")
        )
            .from(DYNAMICS)
            .where(
                DYNAMICS.W.between(dateFrom, dateTo)
            )
            .and(DYNAMICS.SHORT_NAME.`in`(projects.splitToList()))
            .groupBy(DYNAMICS.W)
            .fetchInto(Dynamics::class.java)
    }

    override fun getCommercialProjects(): List<YouTrackProject> {
        return dsl.select(
            PROJECTS.NAME.`as`("name"),
            PROJECTS.SHORT_NAME.`as`("shortName"),
            PROJECTS.ID.`as`("id")
        )
            .from(PROJECTS)
            .leftJoin(projectTypesTable).on(PROJECTS.SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where(projectTypesTable.IS_PUBLIC.eq(true))
            .or(projectTypesTable.IS_PUBLIC.isNull)
            .fetchInto(YouTrackProject::class.java)
    }

    override fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any {
        val projectsCondition = if (issueFilter.projects.isEmpty()) DSL.trueCondition() else DSL.and(issuesTable.PROJECT_SHORT_NAME.`in`(issueFilter.projects))
        val typesCondition: Condition = if (issueFilter.types.isEmpty()) DSL.trueCondition() else DSL.and(typesTable.FIELD_VALUE.`in`(issueFilter.types))
        val statesCondition: Condition = if (issueFilter.states.isEmpty()) DSL.trueCondition() else DSL.and(statesTable.FIELD_VALUE.`in`(issueFilter.states))
        return dsl.select(issuesTable.ID)
            .from(issuesTable)
            .leftJoin(typesTable).on(typesTable.ISSUE_ID.eq(issuesTable.ID).and(typesTable.FIELD_NAME.eq("Type")))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID).and(statesTable.FIELD_NAME.eq("State")))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            /*.and(issuesTable.RESOLVED_DATE.isNull)*/
            .and(typesCondition)
            .and(statesCondition)
            .and(projectsCondition)
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and((((DSL.coalesce(issuesTable.TIME_AGENT, 0) + DSL.coalesce(issuesTable.TIME_DEVELOPER, 0)) / 32400) + 1).eq(days.toLong()))
            .orderBy(issuesTable.CREATED_DATE.asc())
            .fetchInto(String::class.java)
    }

    override fun updateIssueSpentTimeById(issueId: String?) {
        if (issueId == null) return
        try {
            dsl.execute(
                """
                update issues
                set time_user     = a.time_user
                  , time_agent    = a.time_agent
                  , time_developer= a.time_developer
                from (select sum(case when transition_owner = 'YouTrackUser' then time_spent else 0 end)          as time_user
                           , sum(case when transition_owner in ('Agent', 'Undefined') then time_spent else 0 end) as time_agent
                           , sum(case when transition_owner = 'Developer' then time_spent else 0 end)             as time_developer
                           , issue_id
                      from issue_timeline
                      group by issue_id
                     ) a
                where issues.id = a.issue_id
                and issues.id = '$issueId'
            """.trimIndent()
            )
        } catch (e: Exception) {
            println(e.message)
        }
    }

    override fun getPartnerCustomers(): List<PartnerCustomerPair> {
        return dsl.select(
            PARTNER_CUSTOMERS.FIELD_VALUE.`as`("customer"),
            PARTNER_CUSTOMERS.PROJECT_SHORT_NAME.`as`("project")
        ).from(PARTNER_CUSTOMERS).fetchInto(PartnerCustomerPair::class.java)
    }

    override fun getYouTrackIssuesWithDetails(issueFilter: IssueFilter): List<IssueWiThDetails> {
        val query = dsl
            .select(
                issuesTable.ID.`as`("id"),
                issuesTable.PROJECT_SHORT_NAME.`as`("project"),
                customersTable.FIELD_VALUE.`as`("customer"),
                issuesTable.SUMMARY.`as`("summary"),
                issuesTable.CREATED_DATE_TIME.`as`("created"),
                bugsTable.FIELD_VALUE.`as`("issue"),
                requirementsTable.FIELD_VALUE.`as`("requirement"),
                statesTable.FIELD_VALUE.`as`("state"),
                detailedStatesTable.FIELD_VALUE.`as`("detailedState"),
                stateCommentsTable.field(WORK_ITEMS.DESCRIPTION).`as`("comment"),
                stateCommentsTable.field(WORK_ITEMS.AUTHOR_LOGIN).`as`("comment"),
                prioritiesTable.FIELD_VALUE.`as`("priority"),
                issuesTable.ISSUE_TYPE.`as`("type"),
                assigneesTable.FIELD_VALUE.`as`("assignee"),
                issuesTable.TIME_USER.`as`("timeUser"),
                issuesTable.TIME_AGENT.`as`("timeAgent"),
                issuesTable.TIME_DEVELOPER.`as`("timeDeveloper"),
                issuesTable.SLA_FIRST_RESPONSE_INDEX.`as`("firstResponseViolation"),
                issuesTable.SLA_SOLUTION_INDEX.`as`("solutionViolation"),
                teamsTable.FIELD_VALUE.`as`("team"),
                DSL.field("\"A\".\"TAG\"").`as`("plainTags")
            )
            .from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(customersTable).on(issuesTable.ID.eq(customersTable.ISSUE_ID)).and(customersTable.FIELD_NAME.eq("Заказчик"))
            .leftJoin(assigneesTable).on(issuesTable.ID.eq(assigneesTable.ISSUE_ID)).and(assigneesTable.FIELD_NAME.eq("Assignee"))
            .leftJoin(bugsTable).on(issuesTable.ID.eq(bugsTable.ISSUE_ID)).and(bugsTable.FIELD_NAME.eq("Issue"))
            .leftJoin(requirementsTable).on(issuesTable.ID.eq(requirementsTable.ISSUE_ID)).and(requirementsTable.FIELD_NAME.eq("Requirement"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(detailedStatesTable).on(issuesTable.ID.eq(detailedStatesTable.ISSUE_ID)).and(detailedStatesTable.FIELD_NAME.eq("Детализированное состояние"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .leftJoin(stateCommentsTable).on(issuesTable.ID.eq(stateCommentsTable.field(WORK_ITEMS.ISSUE_ID)))
            .leftJoin(teamsTable).on(issuesTable.ID.eq(teamsTable.ISSUE_ID)).and(teamsTable.FIELD_NAME.eq("Команда"))
            .leftJoin(
                DSL.select(tagsTable.ISSUE_ID.`as`("ISSUE_ID"), DSL.arrayAgg(tagsTable.TAG).`as`("TAG"))
                    .from(tagsTable)
                    .groupBy(tagsTable.ISSUE_ID)
                    .asTable()
                    .`as`("A")
            )
            .on(issuesTable.ID.eq(DSL.field("\"A\".\"ISSUE_ID\"").cast(String::class.java)))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toCustomersCondition())
            .and(issueFilter.toPrioritiesCondition())
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toTagsCondition())
            .limit(issueFilter.limit)
        return query.fetchInto(IssueWiThDetails::class.java)
    }
}
