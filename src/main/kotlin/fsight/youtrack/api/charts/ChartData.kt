package fsight.youtrack.api.charts

import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.DynamicsProcessedByDay.DYNAMICS_PROCESSED_BY_DAY
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.SigmaIntermediatePower
import fsight.youtrack.models.SigmaItem
import fsight.youtrack.models.SigmaResult
import fsight.youtrack.splitToList
import fsight.youtrack.toStartOfDate
import fsight.youtrack.toStartOfWeek
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.math.sqrt

@Service
class ChartData(private val dslContext: DSLContext) : IChartData {
    @Autowired
    private lateinit var pg: IPGProvider

    @Autowired
    private lateinit var dictionariesService: IDictionary

    data class SimpleAggregatedValue(val key: String, val value: Int)

    override fun getDynamicsData(projects: String?, dateFrom: String?, dateTo: String?): List<Dynamics> {
        val p = if (projects == null || projects.isEmpty()) dictionariesService.commercialProjects.joinToString(separator = ",") { it.value } else projects
        val df = dateFrom?.toStartOfWeek() ?: Timestamp.valueOf(LocalDateTime.now()).toStartOfDate()
        val dt = dateTo?.toStartOfWeek() ?: Timestamp.valueOf(LocalDateTime.now()).toStartOfDate()
        return pg.getDynamicsData(projects = p, dateFrom = df, dateTo = dt)
    }

    override fun getSigmaData(projects: String, types: String, states: String, dateFrom: String, dateTo: String): SigmaResult {
        val statesField = CUSTOM_FIELD_VALUES.`as`("statesField")
        val filter = if (projects.isEmpty()) {
            dictionariesService.commercialProjects.map { it.value }
        } else {
            projects.splitToList()
        }
        val typesCondition: Condition = if (types.isEmpty()) DSL.trueCondition() else DSL.and(CUSTOM_FIELD_VALUES.FIELD_VALUE.`in`(types.splitToList()))
        val statesCondition: Condition = if (states.isEmpty()) DSL.trueCondition() else DSL.and(statesField.FIELD_VALUE.`in`(states.splitToList()))
        val items: List<Int> =
            dslContext.select(DSL.coalesce(ISSUES.TIME_AGENT, 0) + DSL.coalesce(ISSUES.TIME_DEVELOPER, 0))
                .from(ISSUES)
                .leftJoin(CUSTOM_FIELD_VALUES)
                .on(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(ISSUES.ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Type")))
                .where(ISSUES.RESOLVED_DATE.lessOrEqual(dateTo.toStartOfDate()))
                .and(ISSUES.RESOLVED_DATE.isNotNull)
                .and(typesCondition)
                .and(ISSUES.PROJECT_SHORT_NAME.`in`(filter))
                .orderBy(ISSUES.CREATED_DATE.desc())
                .limit(100)
                .fetchInto(Int::class.java)
        val sourceAgg =
            items.groupBy { 1 + it / 32400 }.map { item -> SigmaItem(item.key, item.value.size) }
                .sortedBy { it.day }.toList()
        val average = items.asSequence().map { it / 32400 }.average()
        val power = sourceAgg.map {
            SigmaIntermediatePower(
                it.day,
                it.count,
                average.toInt(),
                (average.toInt() - it.day) * (average.toInt() - it.day)
            )
        }
        val p = power.asSequence().map { it.p * it.c }.sum().toDouble()
        val c = power.asSequence().map { it.c }.sum() - 1
        if (c == 0) return SigmaResult(0.0, listOf(SigmaItem(0, 0)))
        val sigma = sqrt(p / c)
        val q = dslContext.select(DSL.coalesce(ISSUES.TIME_AGENT, 0) + DSL.coalesce(ISSUES.TIME_DEVELOPER, 0))
            .from(ISSUES)
            .leftJoin(CUSTOM_FIELD_VALUES).on(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(ISSUES.ID).and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Type")))
            .leftJoin(statesField).on(ISSUES.ID.eq(statesField.ISSUE_ID).and(statesField.FIELD_NAME.eq("State")))
            .where(ISSUES.CREATED_DATE.lessOrEqual(dateTo.toStartOfDate()))
            .and(ISSUES.RESOLVED_DATE.isNull)
            .and(typesCondition)
            .and(statesCondition)
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(filter))
            .orderBy(ISSUES.CREATED_DATE.desc())
        val active = q.fetchInto(Int::class.java).groupBy { 1 + it / 32400 }
            .map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        return SigmaResult(sigma, active)
    }

    override fun getCreatedCountOnWeek(
        projects: String,
        dateFrom: String,
        dateTo: String
    ): List<SimpleAggregatedValue> {
        val filter = if (projects.isEmpty()) dictionariesService.commercialProjects.map { it.value } else projects.splitToList()
        val dt = dateTo.toStartOfWeek()
        return dslContext
            .select(
                ISSUES.PROJECT_SHORT_NAME.`as`("key"),
                DSL.count(ISSUES.PROJECT_SHORT_NAME).`as`("value")
            )
            .from(ISSUES)
            .where(ISSUES.CREATED_WEEK.eq(dt))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(filter))
            .groupBy(ISSUES.PROJECT_SHORT_NAME)
            .fetch()
            .map { SimpleAggregatedValue(it["key"].toString(), it["value"].toString().toInt()) }
            .sortedByDescending { it.value }
    }

    override fun getProcessedDaily(
        projects: String,
        dateFrom: String,
        dateTo: String
    ): Any {
        return dslContext.select(
            DYNAMICS_PROCESSED_BY_DAY.D.`as`("kay"),
            DYNAMICS_PROCESSED_BY_DAY.COUNT.`as`("value")
        )
            .from(DYNAMICS_PROCESSED_BY_DAY)
            .fetchInto(SimpleAggregatedValue::class.java)
    }
}
