package fsight.youtrack.api.charts

import fsight.youtrack.generated.jooq.tables.Dynamics.DYNAMICS
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.models.SigmaIntermediatePower
import fsight.youtrack.models.SigmaItem
import fsight.youtrack.models.SigmaResult
import fsight.youtrack.models.TimeLine
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.sum
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

@Service
@Transactional
class ChartDataImplementation(private val dslContext: DSLContext) : ChartDataService {
    override fun getTimeLineData(projects: String, dateFrom: String, dateTo: String): List<TimeLine> {
        val df = LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dt = LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val filter = projects.removeSurrounding("[", "]").split(",")
        return dslContext.select(
                DYNAMICS.W.`as`("week"),
                sum(DYNAMICS.ACTIVE).`as`("active"),
                sum(DYNAMICS.CREATED).`as`("created"),
                sum(DYNAMICS.RESOLVED).`as`("resolved"))
                .from(DYNAMICS)
                .where(DYNAMICS.W.between(Timestamp.valueOf(df.atStartOfDay()), Timestamp.valueOf(dt.atStartOfDay())))
                .and(DYNAMICS.SHORT_NAME.`in`(filter))
                .groupBy(DYNAMICS.W)
                .fetchInto(TimeLine::class.java)
    }

    override fun getSigmaData(projects: String, dateFrom: String, dateTo: String): SigmaResult {
        val filter = projects.removeSurrounding("[", "]").split(",")
        val dt = LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val items: List<Int> = dslContext.select(DSL.coalesce(ISSUES.TIME_AGENT, 0) + DSL.coalesce(ISSUES.TIME_DEVELOPER, 0))
                .from(ISSUES)
                .where(ISSUES.RESOLVED_DATE.lessOrEqual(Timestamp.valueOf(dt.atStartOfDay())))
                .and(ISSUES.RESOLVED_DATE.isNotNull)
                .and(ISSUES.PROJECT.`in`(filter))
                .orderBy(ISSUES.CREATED_DATE.desc())
                .limit(100)
                .fetchInto(Int::class.java)
        val sourceAgg = items.asSequence().groupBy { 1 + it / 32400 }.map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        sourceAgg.forEach { println(it) }
        val average = items.asSequence().map { it / 32400 }.average()
        val power = sourceAgg.map { it -> SigmaIntermediatePower(it.day, it.count, average.toInt(), (average.toInt() - it.day) * (average.toInt() - it.day)) }
        val p = power.asSequence().map { it.p * it.c }.sum().toDouble()
        val c = power.asSequence().map { it.c }.sum() - 1
        println("$p - $c")
        if (c == 0) return SigmaResult(0.0, listOf(SigmaItem(0, 0)))
        val sigma = sqrt(p / c)
        val active = dslContext.select(DSL.coalesce(ISSUES.TIME_AGENT, 0) + DSL.coalesce(ISSUES.TIME_DEVELOPER, 0))
                .from(ISSUES)
                .where(ISSUES.CREATED_DATE.lessOrEqual(Timestamp.valueOf(dt.atStartOfDay())))
                .and(ISSUES.RESOLVED_DATE.isNull)
                .and(ISSUES.PROJECT.`in`(filter))
                .orderBy(ISSUES.CREATED_DATE.desc())
                .limit(100)
                .fetchInto(Int::class.java).asSequence().groupBy { 1 + it / 32400 }.map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        println(active)
        return SigmaResult(sigma, active)
    }

    override fun getCreatedCountOnWeek(projects: String, dateFrom: String, dateTo: String): List<SimpleAggregatedValue>? {
        val filter = projects.removeSurrounding("[", "]").split(",")
        val dt = LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val df = dt.minusDays(7)
        return dslContext
                .select(
                        ISSUES.PROJECT.`as`("name"),
                        DSL.count(ISSUES.PROJECT).`as`("value")
                )
                .from(ISSUES)
                .where(ISSUES.CREATED_DATE.between(Timestamp.valueOf(df.atStartOfDay())).and(Timestamp.valueOf(dt.atStartOfDay())))
                .and(ISSUES.RESOLVED_DATE.isNull)
                .and(ISSUES.PROJECT.`in`(filter))
                .groupBy(ISSUES.PROJECT)
                .fetchInto(SimpleAggregatedValue::class.java).sortedByDescending { it.value }
    }
}

data class SimpleAggregatedValue(val name: String, val value: Int)