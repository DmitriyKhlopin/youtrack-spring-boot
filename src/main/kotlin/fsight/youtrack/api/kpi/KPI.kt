package fsight.youtrack.api.kpi


import fsight.youtrack.generated.jooq.Tables.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.Tables.ISSUE_HISTORY
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.KpiOverallView.KPI_OVERALL_VIEW
import fsight.youtrack.generated.jooq.tables.SlaViolationsResponsible.SLA_VIOLATIONS_RESPONSIBLE
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.models.sql.Issue
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.sql.Date
import java.time.LocalDate
import java.time.temporal.ChronoField


@Service
class KPI(@Qualifier("pgDataSource") private val db: Database, private val dsl: DSLContext) : IKPI {

    private final val prioritiesMap: HashMap<String, Float> by lazy {
        hashMapOf<String, Float>().also {
            it["Major"] = 1.6f
            it["Normal"] = 1.3f
            it["Minor"] = 1.1f
        }
    }

    private final val evaluationsMap: HashMap<String, Float> by lazy {
        hashMapOf<String, Float>().also {
            it["Отлично"] = 1.5f
            it["Удовлетворительно"] = 1f
            it["Неудовлетворительно"] = 0.5f
        }
    }

    private final val issueTypesMap: HashMap<String, Float> by lazy {
        hashMapOf<String, Float>().also {
            it["Консультация"] = 1.3f
            it["Bug"] = 1.6f
            it["Feature"] = 1.3f
            it["P 5.26"] = 1.1f
            it["PP 7.2 по прайсу"] = 1.3f
            it["PP 7.2 по единицам"] = 1.3f
            it["PP 8 по прайсу"] = 1.3f
            it["PP 8 по единицам"] = 1.3f
            it["PP 8.2 по прайсу"] = 1.3f
            it["PP 8.2 по единицам"] = 1.3f
            it["AP 9 по прайсу"] = 1.3f
            it["AP 9 по единицам"] = 1.3f
            it["PP 9 по прайсу"] = 1.3f
            it["PP 9 по единицам"] = 1.3f
            it["FMP_LIC"] = 1f
        }
    }

    data class D(
            var id: String? = null,
            var firstSlaIndex: String? = null,
            var solutionSlaIndex: String? = null,
            var user: String? = null
    )

    override fun getResult(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<R1> {
        val totalMap = getTotal2(projects, emails, dateFrom, dateTo)
        val violationsMap = getViolations(projects, emails, dateFrom, dateTo).map { it.user to it.count }.toMap()
        val postponementsMap = getPostponements(projects, emails, dateFrom, dateTo).map { it.user to it.count }.toMap()
        val suggestedSolutionsMap =
                getSuggestedSolutions(projects, emails, dateFrom, dateTo).map { it.user to it.count }.toMap()
        val requestedClarificationsMap =
                getRequestedClarifications(projects, emails, dateFrom, dateTo).map { it.user to it.count }.toMap()
        val positiveAssessmentsMap =
                getPerformanceEvaluations(projects, emails, dateFrom, dateTo).map { it.user to it.count }.toMap()

        val users = totalMap.map { it.key }
                .union(violationsMap.map { it.key })
                .union(postponementsMap.map { it.key })
                .union(suggestedSolutionsMap.map { it.key })
                .union(requestedClarificationsMap.map { it.key })
                .union(positiveAssessmentsMap.map { it.key })
                .distinct()

        val intermediate = users.filterNotNull().map {
            R1(
                    user = it,
                    total = totalMap[it] ?: 0,
                    violations = violationsMap[it] ?: 0,
                    postponements = postponementsMap[it] ?: 0,
                    suggestedSolutions = suggestedSolutionsMap[it] ?: 0,
                    requestedClarifications = requestedClarificationsMap[it] ?: 0,
                    positiveAssessment = positiveAssessmentsMap[it] ?: 0
            )
        }
        val totalAgg = intermediate.sumBy { it.total }
        return intermediate.map {
            val fTotal = (it.total.toFloat() / totalAgg)
            val fPostponements = (2 - it.postponements.toFloat() / it.total)
            val fRequestedClarifications = (2 - it.requestedClarifications.toFloat() / it.total)
            val fSuggestedSolutions = (1.5f - it.suggestedSolutions.toFloat() / it.total)
            val fViolations = (it.violations.toFloat() / it.total)
            val fPositiveAssessment = (it.positiveAssessment.toFloat() / it.total)
            it.result = (60 * fTotal + 20 * fPostponements + 20 * fRequestedClarifications + 10 *
                    fSuggestedSolutions + 10 * fPositiveAssessment) / (1 + fViolations * 50)
            it
        }.sortedByDescending { it.result }
    }

    data class ResponsibilityObject(
            var id: String? = null,
            var email: String? = null,
            var duration: Int? = null,
            var rn: Int? = null
    )

    data class R1(
            val user: String,
            val total: Int,
            val violations: Int,
            val postponements: Int,
            val suggestedSolutions: Int,
            val requestedClarifications: Int,
            val positiveAssessment: Int,
            var result: Float = 0.0f
    )

    override fun getTotal(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        //TODO исполнителем является сотрудник, который внёс болше всего ТЗ по заявке
        return dsl.select(
                USERS.FULL_NAME.`as`("user"),
                DSL.count(ISSUES.ID).`as`("count")
        )
                .from(ISSUES)
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Assignee")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.SLA_SOLUTION_DATE_TIME.isNotNull)
                .and(USERS.EMAIL.`in`(emails))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    override fun getTotal2(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): Map<String, Int> {
        //TODO исполнителем является сотрудник, который внёс болше всего ТЗ по заявке
        val q = dsl.select(
                USERS.FULL_NAME.`as`("user"),
                ISSUES.ID.`as`("issueId"),
                DSL.sum(WORK_ITEMS.WI_DURATION).`as`("total")
        )
                .from(WORK_ITEMS)
                .leftJoin(USERS)
                .on(WORK_ITEMS.AUTHOR_LOGIN.eq(USERS.USER_LOGIN))
                .leftJoin(ISSUES).on(WORK_ITEMS.ISSUE_ID.eq(ISSUES.ID))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(USERS.EMAIL.`in`(emails))
                .and((WORK_ITEMS.WORK_NAME.notEqual("Анализ сроков выполнения")).or(WORK_ITEMS.WORK_NAME.isNull))
                .groupBy(USERS.FULL_NAME, ISSUES.ID)

        println(q.sql)
        val r = q.fetchInto(T2::class.java)
        return r.groupBy { it.issueId }
                .map<String?, List<T2>, T2?> { it.value.maxBy<T2?, Int> { j -> j?.total ?: 0 } }
                .groupingBy { it?.user ?: "" }
                .eachCount()
    }

    data class T2(var user: String? = null, var issueId: String? = null, var total: Int? = null)

    override fun getViolations(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        //TODO сотрудник должен быть исполнителем во время нарушения
        return dsl.select(
                USERS.FULL_NAME.`as`("user"),
                DSL.countDistinct(ISSUES.ID).`as`("count")
        )
                .from(ISSUES)
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Assignee")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.SLA_SOLUTION_DATE_TIME.isNotNull)
                .and(USERS.EMAIL.`in`(emails))
                .and(ISSUES.SLA_FIRST_RESPONSE_INDEX.eq("Нарушен").or(ISSUES.SLA_SOLUTION_INDEX.eq("Нарушен")))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    data class T1(var user: String? = null, var count: Int? = null)

    //TODO % самосотоятельно решенных (определяетяс по наличию номера issue/требования)

    //TODO средние ТЗ, считать только по исполнителям из getTotal

    override fun getPostponements(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {

        //TODO перенос засчитывать исполнителю в момент переноса
        return dsl.select(
                USERS.FULL_NAME.`as`("user"),
                DSL.count(ISSUE_HISTORY.NEW_VALUE_STRING).`as`("count")
        )
                .from(ISSUE_HISTORY)
                .leftJoin(ISSUES).on(ISSUE_HISTORY.ISSUE_ID.eq(ISSUES.ID))
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Assignee")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.SLA_SOLUTION_DATE_TIME.isNotNull)
                .and(ISSUE_HISTORY.FIELD_NAME.eq("Дата решения"))
                .and(USERS.EMAIL.`in`(emails))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    override fun getSuggestedSolutions(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        //TODO считать только по исполнителям из getTotal
        return dsl.select(
                USERS.FULL_NAME.`as`("user"),
                DSL.count(ISSUE_HISTORY.NEW_VALUE_STRING).`as`("count")
        )
                .from(ISSUE_HISTORY)
                .leftJoin(ISSUES).on(ISSUE_HISTORY.ISSUE_ID.eq(ISSUES.ID))
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Assignee")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.SLA_SOLUTION_DATE_TIME.isNotNull)
                .and(ISSUE_HISTORY.FIELD_NAME.eq("Состояние"))
                .and(ISSUE_HISTORY.NEW_VALUE_STRING.eq("Ожидает подтверждения"))
                .and(USERS.EMAIL.`in`(emails))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    override fun getRequestedClarifications(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        return dsl.select(
                USERS.FULL_NAME.`as`("user"),
                DSL.count(ISSUE_HISTORY.NEW_VALUE_STRING).`as`("count")
        )
                .from(ISSUE_HISTORY)
                .leftJoin(ISSUES).on(ISSUE_HISTORY.ISSUE_ID.eq(ISSUES.ID))
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Assignee")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.SLA_SOLUTION_DATE_TIME.isNotNull)
                .and(ISSUE_HISTORY.FIELD_NAME.eq("Состояние"))
                .and(ISSUE_HISTORY.NEW_VALUE_STRING.eq("Ожидает ответа"))
                .and(USERS.EMAIL.`in`(emails))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    override fun getPerformanceEvaluations(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {

        return dsl.select(
                USERS.FULL_NAME.`as`("user"),
                DSL.count(ISSUES.ID).`as`("count")
        )
                .from(ISSUES)
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Assignee")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.QUALITY_EVALUATION.`in`(listOf("Отлично", "Удовлетворительно")))
                .and(USERS.EMAIL.`in`(emails))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    override fun getCommercialUtilization(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIssueTimeUtilization(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFoundErrors(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSelfSolved(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<T1> {
        return dsl.select(
                ISSUES.ID.`as`("user"),
                DSL.count(ISSUES.ID).`as`("count")
        )
                .from(ISSUES)
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(ISSUES.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Issue")))
                .leftJoin(USERS)
                .on(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq(USERS.USER_LOGIN))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.SLA_SOLUTION_DATE_TIME.isNotNull)
                .and(USERS.EMAIL.`in`(emails))
                .groupBy(USERS.FULL_NAME)
                .fetchInto(T1::class.java)
    }

    override fun getResult2(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp,
            withDetails: Boolean
    ): Any {
        val issueParticipantsQuery = dsl.select(
                USERS.FULL_NAME.`as`("user"),
                ISSUES.ID.`as`("issueId"),
                DSL.sum(WORK_ITEMS.WI_DURATION).`as`("total")
        )
                .from(WORK_ITEMS)
                .leftJoin(USERS)
                .on(WORK_ITEMS.AUTHOR_LOGIN.eq(USERS.USER_LOGIN))
                .leftJoin(ISSUES).on(WORK_ITEMS.ISSUE_ID.eq(ISSUES.ID))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(USERS.EMAIL.`in`(emails))
                .and((WORK_ITEMS.WORK_NAME.notEqual("Анализ сроков выполнения")).or(WORK_ITEMS.WORK_NAME.isNull))
                .groupBy(USERS.FULL_NAME, ISSUES.ID)
        val issueParticipants = issueParticipantsQuery.fetchInto(T2::class.java)
        val priorityTable = CUSTOM_FIELD_VALUES.`as`("priority")
        val productTable = CUSTOM_FIELD_VALUES.`as`("product")
        val typeTable = CUSTOM_FIELD_VALUES.`as`("type")
        val devOpsIssueTable = CUSTOM_FIELD_VALUES.`as`("devOpsIssue")
        val evaluation = CUSTOM_FIELD_VALUES.`as`("evaluation")

        val issueCustomFieldsValuesQuery = dsl.select(
                ISSUES.ID.`as`("id"),
                priorityTable.FIELD_VALUE.`as`("priority"),
                DSL.coalesce(DSL.coalesce(typeTable.FIELD_VALUE, productTable.FIELD_VALUE), ISSUES.PROJECT_SHORT_NAME).`as`(
                        "type"
                ),
                devOpsIssueTable.FIELD_VALUE.`as`("devOpsIssue"),
                evaluation.FIELD_VALUE.`as`("evaluation")
        )
                .from(ISSUES)
                .leftJoin(priorityTable)
                .on(ISSUES.ID.eq(priorityTable.ISSUE_ID).and(priorityTable.FIELD_NAME.eq("Priority")))
                .leftJoin(typeTable).on(ISSUES.ID.eq(typeTable.ISSUE_ID).and(typeTable.FIELD_NAME.eq("Type")))
                .leftJoin(productTable).on(ISSUES.ID.eq(productTable.ISSUE_ID).and(productTable.FIELD_NAME.eq("Продукт")))
                .leftJoin(devOpsIssueTable)
                .on(ISSUES.ID.eq(devOpsIssueTable.ISSUE_ID).and(devOpsIssueTable.FIELD_NAME.eq("Issue")))
                .leftJoin(evaluation).on(ISSUES.ID.eq(evaluation.ISSUE_ID).and(evaluation.FIELD_NAME.eq("Оценка")))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))

        val issueStats = issueCustomFieldsValuesQuery.fetchInto(Issue::class.java)
                .map { it.id to it }.toMap()

        /*issueStats.forEach { println(it.value) }*/

        val solutionAttempts = dsl.select(
                ISSUES.ID.`as`("issueId"),
                USERS.FULL_NAME.`as`("user"),
                DSL.count().`as`("total")
        )
                .from(ISSUE_HISTORY)
                .leftJoin(ISSUES).on(ISSUE_HISTORY.ISSUE_ID.eq(ISSUES.ID))
                .leftJoin(USERS).on(ISSUE_HISTORY.AUTHOR.eq(USERS.EMAIL))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUE_HISTORY.FIELD_NAME.eq("Состояние"))
                .and(ISSUE_HISTORY.OLD_VALUE_STRING.eq("In Progress"))
                .and(ISSUE_HISTORY.NEW_VALUE_STRING.eq("Ожидает подтверждения"))
                .groupBy(ISSUES.ID, USERS.FULL_NAME)
                .fetchInto(T2::class.java)
                .groupBy { it.issueId }.toMap()

        val clarifications = dsl.select(
                ISSUES.ID.`as`("issueId"),
                USERS.FULL_NAME.`as`("user"),
                DSL.count().`as`("total")
        )
                .from(ISSUE_HISTORY)
                .leftJoin(ISSUES).on(ISSUE_HISTORY.ISSUE_ID.eq(ISSUES.ID))
                .leftJoin(USERS).on(ISSUE_HISTORY.AUTHOR.eq(USERS.EMAIL))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUE_HISTORY.FIELD_NAME.eq("Состояние"))
                .and(ISSUE_HISTORY.OLD_VALUE_STRING.eq("In Progress"))
                .and(ISSUE_HISTORY.NEW_VALUE_STRING.eq("Ожидает ответа"))
                .groupBy(ISSUES.ID, USERS.FULL_NAME)
                .fetchInto(T2::class.java)
                .groupBy { it.issueId }.toMap()

        val postponements = dsl.select(
                ISSUES.ID.`as`("issueId"),
                USERS.FULL_NAME.`as`("user"),
                DSL.count().`as`("total")
        )
                .from(ISSUE_HISTORY)
                .leftJoin(ISSUES).on(ISSUE_HISTORY.ISSUE_ID.eq(ISSUES.ID))
                .leftJoin(USERS).on(ISSUE_HISTORY.AUTHOR.eq(USERS.EMAIL))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUE_HISTORY.FIELD_NAME.eq("Плановая дата предоставления ответа"))
                .groupBy(ISSUES.ID, USERS.FULL_NAME)
                .fetchInto(T2::class.java)
                .groupBy { it.issueId }.toMap()

        val violations = dsl.select(
                SLA_VIOLATIONS_RESPONSIBLE.ID.`as`("issueId"),
                SLA_VIOLATIONS_RESPONSIBLE.TYPE.`as`("type"),
                SLA_VIOLATIONS_RESPONSIBLE.RESPONSIBLE.`as`("agent")
        ).from(SLA_VIOLATIONS_RESPONSIBLE)
                .leftJoin(ISSUES).on(SLA_VIOLATIONS_RESPONSIBLE.ID.eq(ISSUES.ID))
                .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .fetchInto(SLAViolation::class.java)
                .groupBy { it.issueId }.toMap()

        val intermediate = issueParticipants.groupBy { it.issueId }
                .map {
                    KPIResultByIssue(
                            issueId = it.key ?: "",
                            agentTime = it.value.map { i -> i.toAgentPair() },
                            totalTime = it.value.sumBy { i -> i.total ?: 0 },
                            priority = issueStats[it.key]?.priority ?: "Undefined",
                            type = issueStats[it.key]?.type ?: "Undefined",
                            devOpsIssue = issueStats[it.key]?.devOpsIssue ?: "Undefined",
                            evaluation = issueStats[it.key]?.evaluation ?: "Undefined",
                            solutionAttempts = solutionAttempts[it.key]?.sumBy { i -> i.total ?: 0 },
                            solutionAttemptsBy = solutionAttempts[it.key]?.map { i -> i.toAgentPair() },
                            clarifications = clarifications[it.key]?.sumBy { i -> i.total ?: 0 },
                            clarificationsBy = clarifications[it.key]?.map { i -> i.toAgentPair() },
                            postponements = postponements[it.key]?.sumBy { i -> i.total ?: 0 },
                            postponementsBy = postponements[it.key]?.map { i -> i.toAgentPair() },
                            slaViolation = violations[it.key]
                    )
                }

        val agents = intermediate.map { it.agentTime.map { k -> k.agent }.toList() }
                .toList().flatten().distinct()
                .map { it to intermediate.filter { i -> i.agentTime.any { j -> j.agent == it } } }


        val ii = agents.map { it.first to it.second.map { i -> i.toKPIScore(it.first) } }
        return ii.aggregate(withDetails)
    }

    fun generateDateRanges(dateTo: Timestamp): List<Pair<Timestamp, Timestamp>> {
        val month = Date(dateTo.time).toLocalDate().monthValue
        val i = Date(dateTo.time).toLocalDate().plusMonths((3 - if (month % 3 == 0) 3 else month % 3).toLong()).with(ChronoField.DAY_OF_MONTH, 1)
        return (0..3).mapIndexed { index, _ -> Pair(i.minusMonths(3L * (index + 1) - 1).toTimestamp(), i.minusMonths(3L * index - 1).minusDays(1).toTimestamp()) }
    }

    fun LocalDate.toTimestamp(): Timestamp = Timestamp.valueOf(this.atStartOfDay())

    override fun getOverallResult(projects: List<String>, emails: List<String>, dateFrom: Timestamp, dateTo: Timestamp): Any {
        val dynamics = generateDateRanges(dateTo).reversed().map {
            val r = dsl.select(
                    DSL.count(KPI_OVERALL_VIEW.ID).`as`("total"),
                    (DSL.count(KPI_OVERALL_VIEW.ID) - DSL.sum(KPI_OVERALL_VIEW.VIOLATED)).`as`("notViolated"),
                    DSL.sum(KPI_OVERALL_VIEW.SATISFIED).`as`("satisfied"),
                    DSL.sum(KPI_OVERALL_VIEW.SELF_SOLVED).`as`("selfSolved"),
                    DSL.sum(KPI_OVERALL_VIEW.SINGLE_SOLUTION).`as`("singleSolution")
            )
                    .from(KPI_OVERALL_VIEW)
                    .leftJoin(ISSUES).on(KPI_OVERALL_VIEW.ID.eq(ISSUES.ID))
                    .where(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                    .and(ISSUES.RESOLVED_DATE.between(it.first).and(it.second))
                    .fetchOneInto(KPIOverallResult::class.java)
            Pair(it, r)
        }
        println(dynamics)

        val result = dsl.select(
                DSL.count(KPI_OVERALL_VIEW.ID).`as`("total"),
                (DSL.count(KPI_OVERALL_VIEW.ID) - DSL.sum(KPI_OVERALL_VIEW.VIOLATED)).`as`("notViolated"),
                DSL.sum(KPI_OVERALL_VIEW.SATISFIED).`as`("satisfied"),
                DSL.sum(KPI_OVERALL_VIEW.SELF_SOLVED).`as`("selfSolved"),
                DSL.sum(KPI_OVERALL_VIEW.SINGLE_SOLUTION).`as`("singleSolution")
        )
                .from(KPI_OVERALL_VIEW)
                .leftJoin(ISSUES).on(KPI_OVERALL_VIEW.ID.eq(ISSUES.ID))
                .where(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
                .and(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
                .fetchOneInto(KPIOverallResult::class.java)
        result.dynamics = dynamics
        return result
    }

    data class KPIOverallResult(
            var total: Int?,
            var notViolated: Int?,
            var satisfied: Int?,
            var selfSolved: Int?,
            var singleSolution: Int?,
            var dynamics: List<Any>?
    )

    data class SLAViolation(
            var issueId: String? = null,
            var type: String? = null,
            var agent: String? = null

    )

    fun T2.toAgentPair() = AgentPair(agent = this.user ?: "", value = this.total ?: 0)

    data class AgentPair(var agent: String, var value: Int)

    data class KPIResultByIssue(
            var issueId: String,
            var agentTime: List<AgentPair>,
            var totalTime: Int,
            var priority: String,
            var type: String,
            var devOpsIssue: String,
            var evaluation: String,
            var solutionAttempts: Int? = null,
            var solutionAttemptsBy: List<AgentPair>? = null,
            var clarifications: Int? = null,
            var clarificationsBy: List<AgentPair>? = null,
            var postponements: Int? = null,
            var postponementsBy: List<AgentPair>? = null,
            var slaViolation: List<SLAViolation>? = null
    )

    fun KPIResultByIssue.toKPIScore(agent: String): KPIScore {
        val percentage = (this.agentTime.firstOrNull { it.agent == agent }?.value?.toFloat() ?: 0f) / this.agentTime.sumBy { it.value }
        val issueTypeScore = issueTypesMap[this.type] ?: 0f
        val priorityScore = prioritiesMap[this.priority] ?: 0f

        val levelScore = when {
            this.type == "Консультация" -> 0f
            this.issueId.startsWith("PP_Lic") || this.issueId.startsWith("FMP_LIC") -> 0f
            this.devOpsIssue == "null" || this.devOpsIssue.isEmpty() -> 1.4f
            else -> 0.6f
        }

        val solutionScore = when {
            solutionAttemptsBy?.none { it.agent == agent } ?: true -> 0f
            levelScore == 1.4f && solutionAttemptsBy?.firstOrNull { it.agent == agent }?.value == 1 -> 1.8f
            solutionAttemptsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 1..2 -> 1.4f
            else -> 0f
        }
        val slaScore = when {
            this.slaViolation?.any { it.agent == agent } ?: false -> 1f
            else -> 1f
        }
        val evaluationsScore = evaluationsMap[this.evaluation] ?: 0.8f
        val postponementsScore = when {
            postponementsBy?.firstOrNull { it.agent == agent }?.value == 0 -> 1f
            postponementsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 1..2 -> 1f
            postponementsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 3..4 -> 0.8f
            postponementsBy?.firstOrNull { it.agent == agent }?.value ?: 0 > 4 -> 0.2f
            else -> 1f
        }
        val clarificationsScore = when {
            clarificationsBy?.firstOrNull { it.agent == agent }?.value == 0 -> 1f
            clarificationsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 1..2 -> 1f
            clarificationsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 3..4 -> 0.8f
            clarificationsBy?.firstOrNull { it.agent == agent }?.value ?: 0 > 4 -> 0.2f
            else -> 1f
        }
        return KPIScore(
                issueId = this.issueId,
                agent = agent,
                percentage = percentage,
                total = percentage * (issueTypeScore + priorityScore + levelScore + solutionScore + evaluationsScore + postponementsScore + clarificationsScore),
                issueType = issueTypeScore,
                priority = priorityScore,
                level = levelScore,
                solution = solutionScore,
                sla = slaScore,
                evaluation = evaluationsScore,
                postponements = postponementsScore,
                clarifications = clarificationsScore,
                violations = if (this.slaViolation?.any { it.agent == agent } == true) 1 else 0
        )
    }

    fun List<Pair<String, List<KPIScore>>>.aggregate(withDetails: Boolean): Any {
        return this.map {
            val total = it.second.sumByDouble { l -> l.total.toDouble() }.toFloat()
            val violations = it.second.sumBy { l -> l.violations }
            val issueTypeSum =
                    it.second.sumByDouble { l -> l.issueType.toDouble() * l.percentage.toDouble() }.toFloat()
            val prioritySum = it.second.sumByDouble { l -> l.priority.toDouble() * l.percentage.toDouble() }.toFloat()
            val levelSum = it.second.sumByDouble { l -> l.level.toDouble() * l.percentage.toDouble() }.toFloat()
            val solutionSum = it.second.sumByDouble { l -> l.solution.toDouble() * l.percentage.toDouble() }.toFloat()
            val slaSum = it.second.sumByDouble { l -> l.sla.toDouble() * l.percentage.toDouble() }.toFloat()
            val evaluationSum =
                    it.second.sumByDouble { l -> l.evaluation.toDouble() * l.percentage.toDouble() }.toFloat()
            val postponementSum =
                    it.second.sumByDouble { l -> l.postponements.toDouble() * l.percentage.toDouble() }.toFloat()
            val clarificationSum =
                    it.second.sumByDouble { l -> l.clarifications.toDouble() * l.percentage.toDouble() }.toFloat()

            val issueTypeAvg =
                    it.second.map { l -> l.issueType }.average().toFloat()
            val priorityAvg = it.second.map { l -> l.priority }.average().toFloat()
            val levelAvg = it.second.map { l -> l.level }.average().toFloat()
            val solutionAvg = it.second.map { l -> l.solution }.average().toFloat()
            val slaAvg = it.second.map { l -> l.sla }.average().toFloat()
            val evaluationAvg =
                    it.second.map { l -> l.evaluation }.average().toFloat()
            val postponementAvg =
                    it.second.map { l -> l.postponements }.average().toFloat()
            val clarificationAvg =
                    it.second.map { l -> l.clarifications }.average().toFloat()

            val issueTypeCount = it.second.map { l -> l.issueType }.filter { l -> l != 0.0f }.count()
            val priorityCount = it.second.map { l -> l.priority }.filter { l -> l != 0.0f }.count()
            val levelCount = it.second.map { l -> l.level }.filter { l -> l != 0.0f }.count()
            val solutionCount = it.second.map { l -> l.solution }.filter { l -> l != 0.0f }.count()
            val slaCount = it.second.map { l -> l.sla }.filter { l -> l != 0.0f }.count()
            val evaluationCount = it.second.map { l -> l.evaluation }.filter { l -> l != 0.0f }.count()
            val postponementCount = it.second.map { l -> l.postponements }.filter { l -> l != 0.0f }.count()
            val clarificationCount = it.second.map { l -> l.clarifications }.filter { l -> l != 0.0f }.count()

            val multiplier = if (violations != 0) 0.8f / violations else 1.0f
            KPIAggregatedScore(
                    agent = it.first,
                    total = total,
                    totalAvg = total / it.second.size,
                    totalWithViolations = total * multiplier,
                    issueTypeSum = issueTypeSum,
                    prioritySum = prioritySum,
                    levelSum = levelSum,
                    solutionSum = solutionSum,
                    slaSum = slaSum,
                    evaluationSum = evaluationSum,
                    postponementSum = postponementSum,
                    clarificationSum = clarificationSum,
                    issueTypeAvg = issueTypeAvg,
                    priorityAvg = priorityAvg,
                    levelAvg = levelAvg,
                    solutionAvg = solutionAvg,
                    slaAvg = slaAvg,
                    evaluationAvg = evaluationAvg,
                    postponementAvg = postponementAvg,
                    clarificationAvg = clarificationAvg,
                    issueTypeCount = issueTypeCount,
                    priorityCount = priorityCount,
                    levelCount = levelCount,
                    solutionCount = solutionCount,
                    slaCount = slaCount,
                    evaluationCount = evaluationCount,
                    postponementCount = postponementCount,
                    clarificationCount = clarificationCount,
                    violations = violations,
                    details = if (withDetails) it.second else listOf()
            )
        }.sortedByDescending { it.totalAvg }
    }

    data class KPIScore(
            val issueId: String,
            val agent: String,
            val percentage: Float,
            val total: Float,
            val issueType: Float,
            val priority: Float,
            val level: Float,
            val solution: Float,
            val sla: Float,
            val evaluation: Float,
            val postponements: Float,
            val clarifications: Float,
            val violations: Int
    )

    data class KPIAggregatedScore(
            val agent: String,
            val total: Float,
            val totalAvg: Float,
            val totalWithViolations: Float,
            val issueTypeSum: Float,
            val prioritySum: Float,
            val levelSum: Float,
            val solutionSum: Float,
            val slaSum: Float,
            val evaluationSum: Float,
            val postponementSum: Float,
            val clarificationSum: Float,
            val issueTypeAvg: Float,
            val priorityAvg: Float,
            val levelAvg: Float,
            val solutionAvg: Float,
            val slaAvg: Float,
            val evaluationAvg: Float,
            val postponementAvg: Float,
            val clarificationAvg: Float,
            val issueTypeCount: Int,
            val priorityCount: Int,
            val levelCount: Int,
            val solutionCount: Int,
            val slaCount: Int,
            val evaluationCount: Int,
            val postponementCount: Int,
            val clarificationCount: Int,
            val violations: Int,
            val details: List<KPIScore>
    )
}
