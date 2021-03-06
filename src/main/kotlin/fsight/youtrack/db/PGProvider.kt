package fsight.youtrack.db

import com.google.gson.Gson
import fsight.youtrack.HookTypes
import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.api.issues.IssueWiThDetails
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.AreaTeam.AREA_TEAM
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.DictionaryProjectCustomerEts.DICTIONARY_PROJECT_CUSTOMER_ETS
import fsight.youtrack.generated.jooq.tables.DynamicsWithTypes.DYNAMICS_WITH_TYPES
import fsight.youtrack.generated.jooq.tables.EtsNames.ETS_NAMES
import fsight.youtrack.generated.jooq.tables.ExtraWorkDays.EXTRA_WORK_DAYS
import fsight.youtrack.generated.jooq.tables.Holidays.HOLIDAYS
import fsight.youtrack.generated.jooq.tables.Hooks.HOOKS
import fsight.youtrack.generated.jooq.tables.IssueTags.ISSUE_TAGS
import fsight.youtrack.generated.jooq.tables.IssueTimeline.ISSUE_TIMELINE
import fsight.youtrack.generated.jooq.tables.IssueTimelineDetailed.ISSUE_TIMELINE_DETAILED
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.LastUserCommentUnresolved.LAST_USER_COMMENT_UNRESOLVED
import fsight.youtrack.generated.jooq.tables.PartnerCustomers.PARTNER_CUSTOMERS
import fsight.youtrack.generated.jooq.tables.ProductOwners.PRODUCT_OWNERS
import fsight.youtrack.generated.jooq.tables.ProjectType.PROJECT_TYPE
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.generated.jooq.tables.StabilizationIndicator_0.STABILIZATION_INDICATOR_0
import fsight.youtrack.generated.jooq.tables.StabilizationIndicator_1.STABILIZATION_INDICATOR_1
import fsight.youtrack.generated.jooq.tables.StateTransitions.STATE_TRANSITIONS
import fsight.youtrack.generated.jooq.tables.Weeks.WEEKS
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.generated.jooq.tables.records.AreaTeamRecord
import fsight.youtrack.generated.jooq.tables.records.CustomFieldValuesRecord
import fsight.youtrack.generated.jooq.tables.records.ProductOwnersRecord
import fsight.youtrack.models.*
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.models.sql.StabilizationIndicator0
import fsight.youtrack.models.sql.StabilizationIndicator1
import fsight.youtrack.models.web.SimpleAggregatedValue1
import fsight.youtrack.models.web.SimpleAggregatedValue2
import fsight.youtrack.splitToList
import fsight.youtrack.toStartOfDate
import fsight.youtrack.toStartOfWeek
import org.jooq.DSLContext
import org.jooq.DatePart
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class PGProvider(private val dsl: DSLContext) : IPGProvider {
    private val issuesTable = ISSUES.`as`("issuesTable")
    private val weeksTable = WEEKS.`as`("weeksTable")
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
    private val etsProjectTable = DICTIONARY_PROJECT_CUSTOMER_ETS.`as`("etsProjectTable")
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
    private fun IssueFilter.toTagsCondition() = if (this.tags.isEmpty()) DSL.trueCondition() else DSL.and(
        DSL.`val`(this.tags.size).eq(DSL.selectCount().from(tagsTable).where(tagsTable.TAG.`in`(this.tags)).and(tagsTable.ISSUE_ID.eq(issuesTable.ID)))
    )

    private fun IssueFilter.toCreateDateCondition() =
        if (this.dateFrom != null && this.dateTo != null) DSL.and(issuesTable.CREATED_DATE.between(this.dateFrom?.toStartOfDate()).and(this.dateTo?.toStartOfDate()))
        else DSL.trueCondition()


    override fun saveHookToDatabase(
        body: Hook?,
        fieldState: String?,
        fieldDetailedState: String?,
        errorMessage: String?,
        inferredState: String?,
        commands: ArrayList<String>?,
        type: HookTypes,
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
            .set(HOOKS.TYPE, type.name)
            .set(HOOKS.RULE, rule?.toString())
            .returning(HOOKS.RECORD_DATE_TIME)
            .fetchOne().recordDateTime
    }


    override fun getDevOpsAssignees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            DSL.coalesce(ETS_NAMES.ETS_NAME, "undefined").`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")
        )
            .from(ETS_NAMES)
            .fetchInto(ETSNameRecord::class.java)
    }

    override fun getSupportEmployees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            DSL.coalesce(ETS_NAMES.ETS_NAME, "undefined").`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")
        )
            .from(ETS_NAMES)
            .where(ETS_NAMES.SUPPORT)
            .fetchInto(ETSNameRecord::class.java)
    }

    override fun getFirstLineEmployees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            DSL.coalesce(ETS_NAMES.ETS_NAME, "undefined").`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")
        )
            .from(ETS_NAMES)
            .where(ETS_NAMES.LINE.eq(1))
            .fetchInto(ETSNameRecord::class.java)
    }

    override fun getFirstLineLeadEmployees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            DSL.coalesce(ETS_NAMES.ETS_NAME, "undefined").`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")
        )
            .from(ETS_NAMES)
            .where(ETS_NAMES.LINE.eq(11))
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

    override fun getDynamicsData(issueFilter: IssueFilter): List<Dynamics> {
        val df = issueFilter.dateFrom?.toStartOfWeek() ?: return listOf()
        val dt = issueFilter.dateTo?.toStartOfWeek() ?: return listOf()
        val projectFilter = if (issueFilter.projects.isEmpty()) trueCondition() else and(DYNAMICS_WITH_TYPES.SHORT_NAME.`in`(issueFilter.projects))
        val typesFilter = if (issueFilter.types.isEmpty()) trueCondition() else and(DYNAMICS_WITH_TYPES.TYPE.`in`(issueFilter.types))
        return dsl.select(
            DYNAMICS_WITH_TYPES.W.`as`("week"),
            sum(DYNAMICS_WITH_TYPES.ACTIVE).`as`("active"),
            sum(DYNAMICS_WITH_TYPES.CREATED).`as`("created"),
            sum(DYNAMICS_WITH_TYPES.RESOLVED).`as`("resolved")
        )
            .from(DYNAMICS_WITH_TYPES)
            .leftJoin(projectTypesTable).on(DYNAMICS_WITH_TYPES.SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and(DYNAMICS_WITH_TYPES.W.between(df, dt))
            .and(projectTypesTable.IS_PUBLIC.eq(true).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(projectFilter)
            .and(typesFilter)
            .groupBy(DYNAMICS_WITH_TYPES.W)
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

    override fun getInnerProjects(): List<YouTrackProject> {
        return dsl.select(
            PROJECTS.NAME.`as`("name"),
            PROJECTS.SHORT_NAME.`as`("shortName"),
            PROJECTS.ID.`as`("id")
        )
            .from(PROJECTS)
            .leftJoin(projectTypesTable).on(PROJECTS.SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where(projectTypesTable.IS_PUBLIC.eq(false))
            .fetchInto(YouTrackProject::class.java)
    }

    override fun getIssuesBySigmaValue(days: Int, issueFilter: IssueFilter): Any {
        return dsl.select(issuesTable.ID)
            .from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(typesTable.ISSUE_ID.eq(issuesTable.ID).and(typesTable.FIELD_NAME.eq("Type")))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID).and(statesTable.FIELD_NAME.eq("State")))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and(issuesTable.RESOLVED_DATE.isNull)
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toPrioritiesCondition())
            .and(issueFilter.toProjectsCondition())
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and((((DSL.coalesce(issuesTable.TIME_AGENT, 0) + DSL.coalesce(issuesTable.TIME_DEVELOPER, 0)) / 540) + 1).eq(days.toLong()))
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
                from (
                    select
                        sum(case when transition_owner = 0 then time_spent_m else 0 end)           as time_user
                        , sum(case when transition_owner in (1, 2, -1) then time_spent_m else 0 end) as time_agent
                        , sum(case when transition_owner = 3 then time_spent_m else 0 end)           as time_developer
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

    override fun updateAllIssuesSpentTime() {
        dsl.execute(
            """
                update issues
                set time_user     = a.time_user
                  , time_agent    = a.time_agent
                  , time_developer= a.time_developer
                from (
                    select
                        sum(case when transition_owner = 0 then time_spent_m else 0 end)           as time_user
                        , sum(case when transition_owner in (1, 2, -1) then time_spent_m else 0 end) as time_agent
                        , sum(case when transition_owner = 3 then time_spent_m else 0 end)           as time_developer
                        , issue_id
                    from issue_timeline
                        group by issue_id
                     ) a
                where issues.id = a.issue_id
            """.trimIndent()
        )
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

    override fun getAreasWithTeams(): List<AreaTeamRecord> {
        return dsl.select(AREA_TEAM.AREA, AREA_TEAM.TEAM).from(AREA_TEAM).fetchInto(AreaTeamRecord::class.java)
    }

    override fun getProductOwners(): List<ProductOwnersRecord> {
        return dsl.select(PRODUCT_OWNERS.TEAM, PRODUCT_OWNERS.OWNER).from(PRODUCT_OWNERS).fetchInto(ProductOwnersRecord::class.java)
    }

    override fun getIssuesUpdatedInPeriod(dateFrom: Timestamp, dateTo: Timestamp): List<String> {
        return dsl
            .select(issuesTable.ID)
            .from(issuesTable)
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and(issuesTable.UPDATED_DATE.between(dateFrom).and(dateTo))
            .and(projectTypesTable.IS_PUBLIC.eq(true).or(projectTypesTable.IS_PUBLIC.isNull))
            .orderBy(issuesTable.UPDATED_DATE.asc())
            .fetchInto(String::class.java)
    }

    override fun getIssueStateTransitionsById(issueId: String): List<IssueTimelineItem> {
        return dsl.select(
            STATE_TRANSITIONS.RN.`as`("order"),
            STATE_TRANSITIONS.ISSUE_ID.`as`("id"),
            STATE_TRANSITIONS.OLD_DT.`as`("dateFrom"),
            STATE_TRANSITIONS.NEW_DT.`as`("dateTo"),
            STATE_TRANSITIONS.O.`as`("stateOld"),
            STATE_TRANSITIONS.N.`as`("stateNew"),
            STATE_TRANSITIONS.OWNER.`as`("stateOwner")
        )
            .from(STATE_TRANSITIONS)
            .where(STATE_TRANSITIONS.ISSUE_ID.eq(issueId))
            .fetchInto(IssueTimelineItem::class.java)
    }

    override fun getIssuesTimelineById(issueId: String): List<IssueTimelineItem> {
        return dsl.select(
            ISSUE_TIMELINE.RN.`as`("order"),
            ISSUE_TIMELINE.ISSUE_ID.`as`("id"),
            ISSUE_TIMELINE.STATE_FROM.`as`("stateOld"),
            ISSUE_TIMELINE.STATE_TO.`as`("stateNew"),
            ISSUE_TIMELINE.DATE_FROM.`as`("dateFrom"),
            ISSUE_TIMELINE.DATE_TO.`as`("dateTo"),
            ISSUE_TIMELINE.TIME_SPENT_M.`as`("timeSpent"),
            ISSUE_TIMELINE.TRANSITION_OWNER.`as`("stateOwner")
        )
            .from(ISSUE_TIMELINE)
            .where(ISSUE_TIMELINE.ISSUE_ID.eq(issueId))
            .fetchInto(IssueTimelineItem::class.java)
    }

    override fun getIssuesDetailedTimelineById(issueId: String): List<IssueTimelineItem> {
        return dsl.select(
            ISSUE_TIMELINE_DETAILED.RN.`as`("order"),
            ISSUE_TIMELINE_DETAILED.ISSUE_ID.`as`("id"),
            ISSUE_TIMELINE_DETAILED.STATE_FROM.`as`("stateOld"),
            ISSUE_TIMELINE_DETAILED.STATE_TO.`as`("stateNew"),
            ISSUE_TIMELINE_DETAILED.DATE_FROM.`as`("dateFrom"),
            ISSUE_TIMELINE_DETAILED.DATE_TO.`as`("dateTo"),
            ISSUE_TIMELINE_DETAILED.TIME_SPENT_M.`as`("timeSpent"),
            ISSUE_TIMELINE_DETAILED.TRANSITION_OWNER.`as`("stateOwner")
        )
            .from(ISSUE_TIMELINE_DETAILED)
            .where(ISSUE_TIMELINE_DETAILED.ISSUE_ID.eq(issueId))
            .fetchInto(IssueTimelineItem::class.java)
    }

    override fun saveIssueTimelineItems(items: List<IssueTimelineItem>): Int {
        return dsl.loadInto(ISSUE_TIMELINE)
            .onDuplicateKeyUpdate()
            .loadRecords(items.map(IssueTimelineItem::toIssueTimelineRecord)).fields(
                ISSUE_TIMELINE.RN,
                ISSUE_TIMELINE.ISSUE_ID,
                ISSUE_TIMELINE.STATE_FROM,
                ISSUE_TIMELINE.STATE_TO,
                ISSUE_TIMELINE.DATE_FROM,
                ISSUE_TIMELINE.DATE_TO,
                ISSUE_TIMELINE.TIME_SPENT_M,
                ISSUE_TIMELINE.TRANSITION_OWNER
            ).execute().stored()
    }

    override fun saveIssueTimelineDetailedItems(items: List<IssueTimelineItem>): Int {
        return dsl.loadInto(ISSUE_TIMELINE_DETAILED)
            .onDuplicateKeyUpdate()
            .loadRecords(items.map(IssueTimelineItem::toIssueDetailedTimelineRecord)).fields(
                ISSUE_TIMELINE_DETAILED.RN,
                ISSUE_TIMELINE_DETAILED.ISSUE_ID,
                ISSUE_TIMELINE_DETAILED.STATE_FROM,
                ISSUE_TIMELINE_DETAILED.STATE_TO,
                ISSUE_TIMELINE_DETAILED.DATE_FROM,
                ISSUE_TIMELINE_DETAILED.DATE_TO,
                ISSUE_TIMELINE_DETAILED.TIME_SPENT_M,
                ISSUE_TIMELINE_DETAILED.TRANSITION_OWNER
            ).execute().stored()
    }

    override fun getVelocity(issueFilter: IssueFilter): List<Velocity> {
        val df = issueFilter.dateFrom?.toStartOfWeek() ?: return listOf()
        val dt = issueFilter.dateTo?.toStartOfWeek() ?: return listOf()
        val t1 = name("t1").fields("week", "type", "result").`as`(
            select(
                issuesTable.RESOLVED_WEEK.`as`("week"),
                DSL.coalesce(typesTable.FIELD_VALUE, "Все типы").`as`("type"),
                DSL.count().`as`("result")
            )
                .from(issuesTable)
                .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
                .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
                .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
                .where()
                .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
                .and(issueFilter.toProjectsCondition())
                .and(issueFilter.toPrioritiesCondition())
                .and(issueFilter.toTypesCondition())
                .and(issuesTable.RESOLVED_WEEK.between(df, dt))
                .groupBy(groupingSets(arrayOf(issuesTable.RESOLVED_WEEK, typesTable.FIELD_VALUE), arrayOf(issuesTable.RESOLVED_WEEK)))
                .orderBy(issuesTable.RESOLVED_WEEK.asc())

        )
        val q = dsl.with(t1).select(
            WEEKS.W.`as`("week"), t1.field("type"), t1.field("result")
        )
            .from(WEEKS)
            .leftJoin(t1).on(WEEKS.W.eq(t1.field("week").cast(Timestamp::class.java)))
            .where()
            .and(WEEKS.W.between(df, dt))
        return q.fetchInto(Velocity::class.java)
    }

    override fun getPrioritiesStats(issueFilter: IssueFilter): List<SimpleAggregatedValue1> {
        return dsl.select(
            prioritiesTable.FIELD_VALUE.`as`("key"),
            count().`as`("value")
        ).from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toCreateDateCondition())
            .groupBy(prioritiesTable.FIELD_VALUE)
            .fetchInto(SimpleAggregatedValue1::class.java)
    }

    override fun getTypesStats(issueFilter: IssueFilter): List<SimpleAggregatedValue1> {
        return dsl.select(
            typesTable.FIELD_VALUE.`as`("key"),
            count().`as`("value")
        ).from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toPrioritiesCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toCreateDateCondition())
            .groupBy(typesTable.FIELD_VALUE)
            .fetchInto(SimpleAggregatedValue1::class.java)
    }

    override fun getAverageLifetime(issueFilter: IssueFilter): List<SimpleAggregatedValue1> {
        return dsl.select(
            coalesce(prioritiesTable.FIELD_VALUE, "Все задачи").`as`("key"),
            (avg(((coalesce(issuesTable.TIME_AGENT, 0) + coalesce(issuesTable.TIME_DEVELOPER, 0)) / 540) + 1)).`as`("value")
        ).from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toTypesCondition())
            /*.and(issueFilter.toStatesCondition())*/
            .and(issueFilter.toCreateDateCondition())
            .and(issuesTable.RESOLVED_DATE.isNotNull)
            .groupBy(groupingSets(arrayOf(prioritiesTable.FIELD_VALUE), arrayOf()))
            .fetchInto(SimpleAggregatedValue1::class.java)
    }


    override fun getAverageLifetimeUnresolved(issueFilter: IssueFilter): List<SimpleAggregatedValue1> {
        return dsl.select(
            coalesce(prioritiesTable.FIELD_VALUE, "Все задачи").`as`("key"),
            (avg(((coalesce(issuesTable.TIME_AGENT, 0) + coalesce(issuesTable.TIME_DEVELOPER, 0)) / 540) + 1)).`as`("value")
        ).from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toCreateDateCondition())
            .groupBy(groupingSets(arrayOf(prioritiesTable.FIELD_VALUE), arrayOf()))
            .fetchInto(SimpleAggregatedValue1::class.java)
    }

    override fun getSigmaReferenceValues(issueFilter: IssueFilter): List<Int> {
        val dt = issueFilter.dateTo?.toStartOfDate() ?: return listOf()
        val q = dsl.select((coalesce(issuesTable.TIME_AGENT, 0) + coalesce(issuesTable.TIME_DEVELOPER, 0) + 540) / 540)
            .from(issuesTable)
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issuesTable.RESOLVED_DATE.lessOrEqual(dt))
            .and(issuesTable.RESOLVED_DATE.isNotNull)
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toPrioritiesCondition())
            .orderBy(issuesTable.CREATED_DATE.desc())
            .limit(100)
        return q.fetchInto(Int::class.java)
    }

    override fun getSigmaActualValues(issueFilter: IssueFilter): List<Int> {
        val df = issueFilter.dateFrom?.toStartOfDate() ?: return listOf()
        val q = dsl.select((coalesce(issuesTable.TIME_AGENT, 0) + coalesce(issuesTable.TIME_DEVELOPER, 0) + 540) / 540)
            .from(issuesTable)
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            /*.and(issuesTable.CREATED_DATE.greaterOrEqual(df))*/
            .and(issuesTable.RESOLVED_DATE.isNull)
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toPrioritiesCondition())
            .orderBy(issuesTable.CREATED_DATE.desc())
        return q.fetchInto(Int::class.java)
    }

    override fun getCreatedOnWeekByPartner(issueFilter: IssueFilter): List<SimpleAggregatedValue1> {
        val dt = issueFilter.dateTo?.toStartOfWeek() ?: return listOf()
        return dsl
            .select(
                issuesTable.PROJECT_SHORT_NAME.`as`("key"),
                count(issuesTable.PROJECT_SHORT_NAME).`as`("value")
            )
            .from(issuesTable)
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toStatesCondition())
            .and(issueFilter.toPrioritiesCondition())
            .and(issueFilter.toCreateDateCondition())
            .groupBy(issuesTable.PROJECT_SHORT_NAME)
            .fetch()
            .map { SimpleAggregatedValue1(null, it["key"].toString(), it["value"].toString().toInt()) }
            .sortedByDescending { it.value }
    }

    override fun getSLAStatsByPriority(issueFilter: IssueFilter): List<SimpleAggregatedValue2> {
        return dsl.select(
            coalesce(prioritiesTable.FIELD_VALUE, "Все приоритеты").`as`("key"),
            sum(
                `when`(issuesTable.SLA_FIRST_RESPONSE_INDEX.eq("Нарушен").or(issuesTable.SLA_SOLUTION_INDEX.eq("Нарушен")), 1)
                    .otherwise(0)
            ).`as`("value1"),
            count().`as`("value2")
        ).from(issuesTable)
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toCreateDateCondition())
            .groupBy(groupingSets(arrayOf(prioritiesTable.FIELD_VALUE), arrayOf()))
            .fetchInto(SimpleAggregatedValue2::class.java)
    }

    override fun getCommercialSLAStatsByPriority(issueFilter: IssueFilter): List<SimpleAggregatedValue2> {
        return dsl.select(
            coalesce(prioritiesTable.FIELD_VALUE, "Все приоритеты").`as`("key"),
            sum(
                `when`(issuesTable.SLA_FIRST_RESPONSE_INDEX.eq("Нарушен").or(issuesTable.SLA_SOLUTION_INDEX.eq("Нарушен")), 1)
                    .otherwise(0)
            ).`as`("value1"),
            count().`as`("value2")
        ).from(issuesTable)
            .leftJoin(customersTable).on(issuesTable.ID.eq(customersTable.ISSUE_ID)).and(customersTable.FIELD_NAME.eq("Заказчик"))
            .leftJoin(etsProjectTable).on(customersTable.FIELD_VALUE.eq(etsProjectTable.CUSTOMER)).and(issuesTable.PROJECT_SHORT_NAME.eq(etsProjectTable.PROJ_SHORT_NAME))
            .and(issuesTable.CREATED_DATE_TIME.between(etsProjectTable.DATE_FROM, etsProjectTable.DATE_TO))
            .leftJoin(prioritiesTable).on(issuesTable.ID.eq(prioritiesTable.ISSUE_ID)).and(prioritiesTable.FIELD_NAME.eq("Priority"))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID)).and(typesTable.FIELD_NAME.eq("Type"))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID)).and(statesTable.FIELD_NAME.eq("State"))
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and(etsProjectTable.PROJ_ETS.similarTo("(FK%|FY%)"))
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and(issueFilter.toProjectsCondition())
            .and(issueFilter.toTypesCondition())
            .and(issueFilter.toCreateDateCondition())
            .groupBy(groupingSets(arrayOf(prioritiesTable.FIELD_VALUE), arrayOf()))
            .fetchInto(SimpleAggregatedValue2::class.java)
    }

    override fun getUnresolvedIssuesForTimelineCalculation(): List<String> {
        return dsl.selectDistinct(issuesTable.ID)
            .from(issuesTable)
            .leftJoin(projectTypesTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypesTable.PROJECT_SHORT_NAME))
            .where()
            .and((projectTypesTable.IS_PUBLIC.eq(true)).or(projectTypesTable.IS_PUBLIC.isNull))
            .and((issuesTable.RESOLVED_DATE_TIME.isNull).or(issuesTable.RESOLVED_DATE_TIME.between(DSL.timestampSub(DSL.now(), 14, DatePart.DAY), DSL.now())))
            .fetchInto(String::class.java)
    }

    override fun getUnresolvedIssuesWithLastCommentByUser(): List<IssueWiThDetails> {
        return dsl.select(
            LAST_USER_COMMENT_UNRESOLVED.ID.`as`("id"),
            LAST_USER_COMMENT_UNRESOLVED.SUMMARY.`as`("summary"),
            LAST_USER_COMMENT_UNRESOLVED.STATE.`as`("state"),
            LAST_USER_COMMENT_UNRESOLVED.DETAILED_STATE.`as`("detailedState"),
            LAST_USER_COMMENT_UNRESOLVED.AUTHOR.`as`("commentAuthor"),
            LAST_USER_COMMENT_UNRESOLVED.CREATED.`as`("created")
        ).from(LAST_USER_COMMENT_UNRESOLVED)
            .orderBy(LAST_USER_COMMENT_UNRESOLVED.CREATED.asc())
            .fetchInto(IssueWiThDetails::class.java)
    }

    override fun getUnresolvedIssues(): List<String> {
        return dsl
            .select(issuesTable.ID)
            .from(issuesTable)
            .where(issuesTable.RESOLVED_DATE_TIME.isNull)
            .fetchInto(String::class.java)
    }

    override fun getHolidays(): List<String> {
        return dsl.select(DSL.cast(HOLIDAYS.HOLIDAY, String::class.java))
            .from(HOLIDAYS)
            .fetchInto(String::class.java)
    }

    override fun getExtraWorkDays(): List<String> {
        return dsl.select(DSL.cast(EXTRA_WORK_DAYS.WORK_DAY, String::class.java))
            .from(EXTRA_WORK_DAYS)
            .fetchInto(String::class.java)
    }

    override fun getStabilizationIndicator0(): List<StabilizationIndicator0> {
        return dsl.select().from(STABILIZATION_INDICATOR_0).fetchInto(StabilizationIndicator0::class.java)
    }

    override fun getStabilizationIndicator1(): List<StabilizationIndicator1> {
        return dsl.select(
            STABILIZATION_INDICATOR_1.Y,
            STABILIZATION_INDICATOR_1.M,
            STABILIZATION_INDICATOR_1.RESULT_DAYS_DIFF,
            STABILIZATION_INDICATOR_1.RESULT_INTERVALS,
            STABILIZATION_INDICATOR_1.SUCCESSFULLY_COMPLETE,
            STABILIZATION_INDICATOR_1.FAILED,
            STABILIZATION_INDICATOR_1.LOW_RISK,
            STABILIZATION_INDICATOR_1.HIGH_RISK
        )
            .from(STABILIZATION_INDICATOR_1)
            .fetchInto(StabilizationIndicator1::class.java)
    }
}
