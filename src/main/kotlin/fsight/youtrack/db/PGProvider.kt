package fsight.youtrack.db

import com.google.gson.Gson
import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Dynamics.DYNAMICS
import fsight.youtrack.generated.jooq.tables.EtsNames.ETS_NAMES
import fsight.youtrack.generated.jooq.tables.Hooks.HOOKS
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.ProjectType.PROJECT_TYPE
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldValuesRecord
import fsight.youtrack.models.Dynamics
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
    private val projectTypeTable = PROJECT_TYPE.`as`("projectTypeTable")
    private val statesTable = CUSTOM_FIELD_VALUES.`as`("statesTable")
    private val typesTable = CUSTOM_FIELD_VALUES.`as`("typesTable")
    private val teamsTable = CUSTOM_FIELD_VALUES.`as`("teamsTable")

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
            .leftJoin(projectTypeTable).on(PROJECTS.SHORT_NAME.eq(projectTypeTable.PROJECT_SHORT_NAME))
            .where(projectTypeTable.IS_PUBLIC.eq(true))
            .or(projectTypeTable.IS_PUBLIC.isNull)
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
            .leftJoin(projectTypeTable).on(issuesTable.PROJECT_SHORT_NAME.eq(projectTypeTable.PROJECT_SHORT_NAME))
            .where()
            /*.and(issuesTable.RESOLVED_DATE.isNull)*/
            .and(typesCondition)
            .and(statesCondition)
            .and(projectsCondition)
            .and((projectTypeTable.IS_PUBLIC.eq(true)).or(projectTypeTable.IS_PUBLIC.isNull))
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
}
