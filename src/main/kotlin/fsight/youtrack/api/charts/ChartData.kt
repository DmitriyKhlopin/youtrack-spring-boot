package fsight.youtrack.api.charts

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.DynamicsProcessedByDay.DYNAMICS_PROCESSED_BY_DAY
import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.SigmaIntermediatePower
import fsight.youtrack.models.SigmaItem
import fsight.youtrack.models.SigmaResult
import fsight.youtrack.models.web.SimpleAggregatedValue1
import fsight.youtrack.models.web.SimpleAggregatedValue2
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.math.sqrt

@Service
class ChartData(private val dslContext: DSLContext) : IChartData {
    @Autowired
    private lateinit var pg: IPGProvider

    override fun getDynamicsData(issueFilter: IssueFilter): List<Dynamics> {
        return pg.getDynamicsData(issueFilter)
    }

    override fun getSigmaData(issueFilter: IssueFilter): SigmaResult {
        val referenceValues = pg.getSigmaReferenceValues(issueFilter)
        val referenceGrouped = referenceValues.groupBy { it }.map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        val referenceAverage = referenceValues.asSequence().map { it / 32400 }.average()
        val power = referenceGrouped.map {
            SigmaIntermediatePower(
                it.day,
                it.count,
                referenceAverage.toInt(),
                (referenceAverage.toInt() - it.day) * (referenceAverage.toInt() - it.day)
            )
        }
        val p = power.asSequence().map { it.p * it.c }.sum().toDouble()
        val c = power.asSequence().map { it.c }.sum() - 1
        if (c == 0) return SigmaResult(0.0, listOf(SigmaItem(0, 0)))
        val sigma = sqrt(p / c)
        val actualValues = pg.getSigmaActualValues(issueFilter)
        val active = actualValues.groupBy { it }.map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        return SigmaResult(sigma, active)
    }

    override fun getCreatedCountOnWeek(
        issueFilter: IssueFilter
    ): List<SimpleAggregatedValue1> {
        return pg.getCreatedOnWeekByPartner(issueFilter)
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
            .fetchInto(SimpleAggregatedValue1::class.java)
    }

    override fun getPrioritiesStats(issueFilter: IssueFilter): Any {
        return pg.getPrioritiesStats(issueFilter).map {
            when (it.key) {
                "Major" -> SimpleAggregatedValue1(1, "Высокий", it.value)
                "Normal" -> SimpleAggregatedValue1(2, "Обычный", it.value)
                "Minor" -> SimpleAggregatedValue1(3, "Низкий", it.value)
                else -> SimpleAggregatedValue1(4, "Не отпределён", it.value)
            }
        }.sortedBy { it.order }
    }

    override fun getTypesStats(issueFilter: IssueFilter): Any {
        return pg.getTypesStats(issueFilter).map {
            when (it.key) {
                "Консультация" -> SimpleAggregatedValue1(1, "Консультация", it.value)
                "Bug" -> SimpleAggregatedValue1(2, "Ошибка", it.value)
                "Feature" -> SimpleAggregatedValue1(3, "Новая функциональность", it.value)
                else -> SimpleAggregatedValue1(4, "Не отпределён", it.value)
            }
        }.sortedBy { it.order }
    }

    override fun getAverageLifetime(issueFilter: IssueFilter): Any {
        return pg.getAverageLifetime(issueFilter).map {
            when (it.key) {
                "Major" -> SimpleAggregatedValue1(2, "Высокий", it.value)
                "Normal" -> SimpleAggregatedValue1(3, "Обычный", it.value)
                "Minor" -> SimpleAggregatedValue1(4, "Низкий", it.value)
                else -> SimpleAggregatedValue1(1, "Все задачи", it.value)
            }
        }.sortedBy { it.order }
    }

    override fun getAverageLifetimeUnresolved(issueFilter: IssueFilter): Any {
        return pg.getAverageLifetimeUnresolved(issueFilter).map {
            when (it.key) {
                "Major" -> SimpleAggregatedValue1(2, "Высокий", it.value)
                "Normal" -> SimpleAggregatedValue1(3, "Обычный", it.value)
                "Minor" -> SimpleAggregatedValue1(4, "Низкий", it.value)
                else -> SimpleAggregatedValue1(1, "Все задачи", it.value)
            }
        }.sortedBy { it.order }
    }

    override fun getSLAStatsByPriority(issueFilter: IssueFilter): Any {
        return pg.getSLAStatsByPriority(issueFilter).map {
            when (it.key) {
                "Major" -> SimpleAggregatedValue2(2, "Высокий", it.value1, it.value2)
                "Normal" -> SimpleAggregatedValue2(3, "Обычный", it.value1, it.value2)
                "Minor" -> SimpleAggregatedValue2(4, "Низкий", it.value1, it.value2)
                else -> SimpleAggregatedValue2(1, "Все задачи", it.value1, it.value2)
            }
        }.sortedBy { it.order }
    }

    override fun getCommercialSLAStatsByPriority(issueFilter: IssueFilter): List<SimpleAggregatedValue2> {
        return pg.getCommercialSLAStatsByPriority(issueFilter).map {
            when (it.key) {
                "Major" -> SimpleAggregatedValue2(2, "Высокий", it.value1, it.value2)
                "Normal" -> SimpleAggregatedValue2(3, "Обычный", it.value1, it.value2)
                "Minor" -> SimpleAggregatedValue2(4, "Низкий", it.value1, it.value2)
                else -> SimpleAggregatedValue2(1, "Все задачи", it.value1, it.value2)
            }
        }.sortedBy { it.order }
    }
}
