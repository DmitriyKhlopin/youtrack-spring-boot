package fsight.youtrack.api.kpi

import fsight.youtrack.generated.jooq.Tables.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.Tables.ISSUE_HISTORY
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service
@Transactional
class KPI(@Qualifier("pgDataSource") private val db: Database, private val dsl: DSLContext) : IKPI {
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
        val total = getTotal(projects, emails, dateFrom, dateTo)
        val violations = getViolations(projects, emails, dateFrom, dateTo)
        val postponements = getPostponements(projects, emails, dateFrom, dateTo)
        val suggestedSolutions = getSuggestedSolutions(projects, emails, dateFrom, dateTo)
        val requestedClarifications = getRequestedClarifications(projects, emails, dateFrom, dateTo)
        val positiveAssessments = getPerformanceEvaluations(projects, emails, dateFrom, dateTo)
        val users = total.map { it -> it.user }.union(violations.map { it -> it.user })
            .union(postponements.map { it -> it.user }).union(suggestedSolutions.map { it -> it.user })
            .union(requestedClarifications.map { it -> it.user }).distinct()

        val totalMap = total.map { it.user to it.count }.toMap()
        val violationsMap = violations.map { it.user to it.count }.toMap()
        val postponementsMap = postponements.map { it.user to it.count }.toMap()
        val suggestedSolutionsMap = suggestedSolutions.map { it.user to it.count }.toMap()
        val requestedClarificationsMap = requestedClarifications.map { it.user to it.count }.toMap()
        val positiveAssessmentsMap = positiveAssessments.map { it.user to it.count }.toMap()

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
        val r = intermediate.map {
            val fTotal = (it.total.toFloat() / totalAgg)
            val fPostponements = (1 - it.postponements.toFloat() / it.total)
            val fRequestedClarifications = (1 - it.requestedClarifications.toFloat() / it.total)
            val fSuggestedSolutions = (1 - it.suggestedSolutions.toFloat() / it.total)
            val fViolations = (it.violations.toFloat() / it.total)
            val fPositiveAssessment = (it.positiveAssessment.toFloat() / it.total)

            println("${it.user} $fTotal $fPostponements $fSuggestedSolutions $fRequestedClarifications $fViolations $fPositiveAssessment")

            it.result = (60 * fTotal + 20 * fPostponements + 20 * fRequestedClarifications + 10 *
                    fSuggestedSolutions + 10 * fPositiveAssessment) / (1 + fViolations * 50)
            /*    */ /*- 100 * fViolations */
            it

        }

        val q = dsl.select(
            ISSUES.ID.`as`("id"),
            USERS.EMAIL.`as`("email"),
            DSL.sum(WORK_ITEMS.WI_DURATION).`as`("duration"),
            DSL.rowNumber().over().partitionBy(ISSUES.ID).orderBy(
                DSL.sum(
                    WORK_ITEMS.WI_DURATION
                )
            ).`as`("rn")
        )
            .from(ISSUES)
            .leftJoin(WORK_ITEMS).on(ISSUES.ID.eq(WORK_ITEMS.ISSUE_ID))
            .leftJoin(USERS).on(WORK_ITEMS.AUTHOR_LOGIN.eq(USERS.USER_LOGIN))
            .where(ISSUES.PROJECT_SHORT_NAME.`in`(projects))
            .and(USERS.EMAIL.`in`(emails))
            .and(
                (ISSUES.CREATED_DATE.between(dateFrom, dateTo)
                    .or(ISSUES.RESOLVED_DATE.between(dateFrom, dateTo))
                    .or(ISSUES.RESOLVED_DATE.isNull))
            )
            .groupBy(ISSUES.ID, USERS.EMAIL)
        println(q.sql)
        println(dateFrom)
        println(dateTo)
        println(emails)
        println(projects)
        val res1 = q.execute()

        println(res1)

        return r.sortedByDescending { it.result }
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

    override fun getViolations(
        projects: List<String>,
        emails: List<String>,
        dateFrom: Timestamp,
        dateTo: Timestamp
    ): List<T1> {
        //TODO сотрудник должен быть исполнителем во время нарушения
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
}
