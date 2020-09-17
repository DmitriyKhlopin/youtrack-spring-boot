package fsight.youtrack.api.kpi


import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.Tables.*
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.KpiOverallView.KPI_OVERALL_VIEW
import fsight.youtrack.generated.jooq.tables.SlaViolationsResponsible.SLA_VIOLATIONS_RESPONSIBLE
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import fsight.youtrack.models.sql.Issue
import fsight.youtrack.splitToList
import fsight.youtrack.toDateRanges
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.sql.Timestamp


@Service
class KPI(@Qualifier("pgDataSource") private val db: Database, private val dsl: DSLContext) : IKPI {
    @Autowired
    private lateinit var pg: IPGProvider


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

    data class BasicKPIIndicatorRecord(var user: String? = null, var issueId: String? = null, var total: Int? = null)

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
        var slaViolation: List<SLAViolation>? = null,
        var commercialSlaViolations: List<SLAViolation>? = null,
        var isCommercial: Boolean? = null
    )


    data class KPIOverallResult(
        var total: Int?,
        var notViolated: Int?,
        var satisfied: Int?,
        var selfSolved: Int?,
        var singleSolution: Int?,
        var dynamics: List<Any>?,
        var totalCommercial: Int?,
        var notViolatedCommercial: Int?,
        var satisfiedCommercial: Int?,
        var selfSolvedCommercial: Int?,
        var singleSolutionCommercial: Int?
    )

    data class SLAViolation(
        var issueId: String? = null,
        var type: String? = null,
        var agent: String? = null,
        var isCommercial: Boolean? = null
    )

    fun BasicKPIIndicatorRecord.toAgentPair() = AgentPair(agent = this.user ?: "", value = this.total ?: 0)


    fun KPIResultByIssue.toKPIScore(agent: String): KPIScore {
        /**Вычисление % трудозатрат, внесенных агентом*/
        val percentage = (this.agentTime.firstOrNull { it.agent == agent }?.value?.toFloat()
            ?: 0f) / this.agentTime.sumBy { it.value }

        /**Вычисление баллов, полученных на основании типа задачи*/
        val issueTypeScore = issueTypesMap[this.type] ?: 0f

        /**Вычисление баллов, полученных на основании приоритета задачи*/
        val priorityScore = prioritiesMap[this.priority] ?: 0f

        /**Вычисление баллов, полученных на основании уровня решения и типа задачи*/
        val levelScore = when {
            this.type == "Консультация" -> 0f
            this.issueId.startsWith("PP_Lic") || this.issueId.startsWith("FMP_LIC") -> 0f
            this.devOpsIssue == "null" || this.devOpsIssue.isEmpty() -> 1.4f
            else -> 0.6f
        }

        /**Вычисление баллов, полученных на основании количества попыток решить задачу*/
        val solutionScore = when {
            solutionAttemptsBy?.none { it.agent == agent } ?: true -> 0f
            levelScore == 1.4f && solutionAttemptsBy?.firstOrNull { it.agent == agent }?.value == 1 -> 1.8f
            solutionAttemptsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 1..2 -> 1.4f
            else -> 0f
        }

        /**Вычисление баллов, полученных на основании наличия нарушения SLA*/
        val slaScore = when {
            this.slaViolation?.any { it.agent == agent } ?: false -> 0f
            else -> 1f
        }

        val commercialSlaScore = when {
            this.commercialSlaViolations?.any { it.agent == agent } ?: false -> 0f
            else -> 1f
        }

        /**Вычисление баллов, полученных на основании оценки*/
        val evaluationsScore = evaluationsMap[this.evaluation] ?: 0.8f

        /**Вычисление баллов, полученных на основании количества переносов сроков ответа*/
        val postponementsScore = when {
            postponementsBy?.firstOrNull { it.agent == agent }?.value == 0 -> 1f
            postponementsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 1..2 -> 1f
            postponementsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 3..4 -> 0.8f
            postponementsBy?.firstOrNull { it.agent == agent }?.value ?: 0 > 4 -> 0.2f
            else -> 1f
        }

        /**Вычисление баллов, полученных на основании количества запросов уточнения*/
        val clarificationsScore = when {
            clarificationsBy?.firstOrNull { it.agent == agent }?.value == 0 -> 1f
            clarificationsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 1..2 -> 1f
            clarificationsBy?.firstOrNull { it.agent == agent }?.value ?: 0 in 3..4 -> 0.8f
            clarificationsBy?.firstOrNull { it.agent == agent }?.value ?: 0 > 4 -> 0.2f
            else -> 1f
        }
        /**Итоговый счёт агента в рамках заявки*/
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

    override fun getResult(
        projects: List<String>,
        emails: String?,
        dateFrom: Timestamp,
        dateTo: Timestamp,
        withDetails: Boolean
    ): Any {
        /**Получаем список сотрудников с номерами задач и трудозатратами сотрудников в рамках этих задач*/

        val e = emails?.splitToList() ?: pg.getSupportEmployees().map { it.email }
        val issueParticipantsQuery = dsl.select(
            USERS.FULL_NAME.`as`("user"),
            ISSUES.ID.`as`("issueId"),
            DSL.sum(WORK_ITEMS.WI_DURATION).`as`("total")
        )
            .from(WORK_ITEMS)
            .leftJoin(USERS).on(WORK_ITEMS.AUTHOR_LOGIN.eq(USERS.USER_LOGIN))
            .leftJoin(ISSUES).on(WORK_ITEMS.ISSUE_ID.eq(ISSUES.ID))
            .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
            .and(USERS.EMAIL.`in`(e))
            .and((WORK_ITEMS.WORK_NAME.notEqual("Анализ сроков выполнения")).or(WORK_ITEMS.WORK_NAME.isNull))
            .groupBy(USERS.FULL_NAME, ISSUES.ID)
        val issueParticipants = issueParticipantsQuery.fetchInto(BasicKPIIndicatorRecord::class.java)
        val priorityTable = CUSTOM_FIELD_VALUES.`as`("priority")
        val productTable = CUSTOM_FIELD_VALUES.`as`("product")
        val typeTable = CUSTOM_FIELD_VALUES.`as`("type")
        val devOpsIssueTable = CUSTOM_FIELD_VALUES.`as`("devOpsIssue")
        val evaluation = CUSTOM_FIELD_VALUES.`as`("evaluation")

        /**Получаем приоритет, тип, список багов и оценки из задач в YT*/
        val issueCustomFieldsValuesQuery = dsl.select(
            ISSUES.ID.`as`("id"),
            priorityTable.FIELD_VALUE.`as`("priority"),
            DSL.coalesce(DSL.coalesce(typeTable.FIELD_VALUE, productTable.FIELD_VALUE), ISSUES.PROJECT_SHORT_NAME).`as`(
                "type"
            ),
            devOpsIssueTable.FIELD_VALUE.`as`("devOpsIssue"),
            evaluation.FIELD_VALUE.`as`("evaluation"),
            COMMERCIAL_ISSUES.COMMERCIAL.`as`("isCommercial")
        )
            .from(ISSUES)
            .leftJoin(priorityTable)
            .on(ISSUES.ID.eq(priorityTable.ISSUE_ID).and(priorityTable.FIELD_NAME.eq("Priority")))
            .leftJoin(typeTable).on(ISSUES.ID.eq(typeTable.ISSUE_ID).and(typeTable.FIELD_NAME.eq("Type")))
            .leftJoin(productTable).on(ISSUES.ID.eq(productTable.ISSUE_ID).and(productTable.FIELD_NAME.eq("Продукт")))
            .leftJoin(devOpsIssueTable)
            .on(ISSUES.ID.eq(devOpsIssueTable.ISSUE_ID).and(devOpsIssueTable.FIELD_NAME.eq("Issue")))
            .leftJoin(evaluation).on(ISSUES.ID.eq(evaluation.ISSUE_ID).and(evaluation.FIELD_NAME.eq("Оценка")))
            .leftJoin(COMMERCIAL_ISSUES).on(ISSUES.ID.eq(COMMERCIAL_ISSUES.ID))
            .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))

        val issueStats = issueCustomFieldsValuesQuery.fetchInto(Issue::class.java)
            .map { it.id to it }.toMap()

        /**Получаем список задач с количеством попыток закрыть задачу для каждого сотрудника*/
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
            .fetchInto(BasicKPIIndicatorRecord::class.java)
            .groupBy { it.issueId }.toMap()

        /**Получаем список задач с количеством запросов уточнений для каждого сотрудника*/
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
            .fetchInto(BasicKPIIndicatorRecord::class.java)
            .groupBy { it.issueId }.toMap()

        /**Получаем список задач с количеством переносов сроков решения для каждого сотрудника*/
        val postponements = dsl.select(
            RESPONSIBLE_FOR_PLANNED_DATE_SHIFTS.ISSUE_ID.`as`("issueId"),
            RESPONSIBLE_FOR_PLANNED_DATE_SHIFTS.ASSIGNEE.`as`("user"),
            RESPONSIBLE_FOR_PLANNED_DATE_SHIFTS.AMMOUNT.`as`("total")
        )
            .from(RESPONSIBLE_FOR_PLANNED_DATE_SHIFTS)
            .leftJoin(ISSUES).on(RESPONSIBLE_FOR_PLANNED_DATE_SHIFTS.ISSUE_ID.eq(ISSUES.ID))
            .leftJoin(USERS).on(RESPONSIBLE_FOR_PLANNED_DATE_SHIFTS.ASSIGNEE.eq(USERS.FULL_NAME))
            .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
            .fetchInto(BasicKPIIndicatorRecord::class.java)
            .groupBy { it.issueId }.toMap()

        /**Получаем список задач с количеством переносов сроков решения для каждого сотрудника*/
        val violations = dsl.select(
            SLA_VIOLATIONS_RESPONSIBLE.ID.`as`("issueId"),
            SLA_VIOLATIONS_RESPONSIBLE.TYPE.`as`("type"),
            SLA_VIOLATIONS_RESPONSIBLE.RESPONSIBLE.`as`("agent"),
            COMMERCIAL_ISSUES.COMMERCIAL.`as`("isCommercial")
        ).from(SLA_VIOLATIONS_RESPONSIBLE)
            .leftJoin(ISSUES).on(SLA_VIOLATIONS_RESPONSIBLE.ID.eq(ISSUES.ID))
            .leftJoin(COMMERCIAL_ISSUES).on(SLA_VIOLATIONS_RESPONSIBLE.ID.eq(COMMERCIAL_ISSUES.ID))
            .where(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
            .fetchInto(SLAViolation::class.java)
            .groupBy { it.issueId }.toMap()

        /**Сливаем все данные по каждой заявке в одну запись
         * Для каждой задачи формируется объект вида
         *  {
         *      "issueId": "EXRP-7",
         *      "agentTime": [{"agent": "Николай Пархачев", "value": 15}, {"agent": "Яна Мальцева", "value": 35}],
         *      "totalTime": 50,
         *      "priority": "Normal",
         *      "type": "Feature",
         *      "devOpsIssue": "null",
         *      "evaluation": "Не оценена",
         *      "solutionAttempts": 1,
         *      "solutionAttemptsBy": [{"agent": "Сагалов Михаил", "value": 1}],
         *      "clarifications": null,
         *      "clarificationsBy": null,
         *      "postponements": 1,
         *      "postponementsBy": [{"agent": "Сагалов Михаил", "value": 1}],
         *      "slaViolation": [{"issueId": "EXRP-7", "type": "solution", "agent": "Николай Пархачев"}]
         * }
         * */
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
                    slaViolation = violations[it.key],
                    commercialSlaViolations = violations[it.key]?.filter { v -> v.isCommercial == true },
                    isCommercial = issueStats[it.key]?.isCommercial ?: false
                )
            }

        /**Из всех объектов с предыдущего шага берутся уникальные исполнители*/
        val agents = intermediate.map { it.agentTime.map { k -> k.agent }.toList() }.toList().flatten().distinct()

        /**Список уникальных исполнителей пополняется информацией о заявках, если исполнитель участвовал в заявке, т.е. вносил трудозатраты*/
        return agents.map { it to intermediate.filter { i -> i.agentTime.any { j -> j.agent == it } }.map { i -> i.toKPIScore(it) } }.aggregate(withDetails)
    }


    override fun getOverallResult(projects: List<String>, dateFrom: Timestamp, dateTo: Timestamp): Any {
        val dynamics = dateTo
            .toDateRanges(4, 3)
            .reversed()
            .map {
                val r = getOverallResultForPeriod(projects, it.first, it.second)
                Pair(it, r)
            }

        val result = getOverallResultForPeriod(projects, dateFrom, dateTo)
        result.dynamics = dynamics
        return result
    }

    fun getOverallResultForPeriod(projects: List<String>, dateFrom: Timestamp, dateTo: Timestamp): KPIOverallResult {
        val q = dsl.select(
            DSL.count(KPI_OVERALL_VIEW.ID).`as`("total"),
            (DSL.count(KPI_OVERALL_VIEW.ID) - DSL.sum(KPI_OVERALL_VIEW.VIOLATED)).`as`("notViolated"),
            DSL.sum(KPI_OVERALL_VIEW.SATISFIED).`as`("satisfied"),
            DSL.sum(KPI_OVERALL_VIEW.SELF_SOLVED).`as`("selfSolved"),
            DSL.sum(KPI_OVERALL_VIEW.SINGLE_SOLUTION).`as`("singleSolution"),
            DSL.sum(KPI_OVERALL_VIEW.COMMERCIAL).`as`("totalCommercial"),
            (DSL.sum(KPI_OVERALL_VIEW.COMMERCIAL) - DSL.sum(KPI_OVERALL_VIEW.COMMERCIAL * KPI_OVERALL_VIEW.VIOLATED)).`as`("notViolatedCommercial"),
            DSL.sum(KPI_OVERALL_VIEW.COMMERCIAL * KPI_OVERALL_VIEW.SATISFIED).`as`("satisfiedCommercial"),
            DSL.sum(KPI_OVERALL_VIEW.COMMERCIAL * KPI_OVERALL_VIEW.SELF_SOLVED).`as`("selfSolvedCommercial"),
            DSL.sum(KPI_OVERALL_VIEW.COMMERCIAL * KPI_OVERALL_VIEW.SINGLE_SOLUTION).`as`("singleSolutionCommercial")
        )
            .from(KPI_OVERALL_VIEW)
            .leftJoin(ISSUES).on(KPI_OVERALL_VIEW.ID.eq(ISSUES.ID))
            .where(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
            .and(ISSUES.RESOLVED_DATE.between(dateFrom).and(dateTo))
        return q.fetchOneInto(KPIOverallResult::class.java)
    }
}
