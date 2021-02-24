package fsight.youtrack.api.charts

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.DynamicsProcessedByDay.DYNAMICS_PROCESSED_BY_DAY
import fsight.youtrack.models.*
import fsight.youtrack.models.web.ComplexAggregatedValue1
import fsight.youtrack.models.web.ComplexAggregatedValue2
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
        println("referenceValues")
        println(referenceValues)
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
        val c = power.asSequence().map { it.c }.sum()/* - 1*/
        /*println(c)*/
        println(power.asSequence().map { it.c })
        if (c == 0) return SigmaResult(0.0, listOf(SigmaItem(0, 0)))
        val sigma = sqrt(p / c)
        val actualValues = pg.getSigmaActualValues(issueFilter)
        println("actualValues")
        println(actualValues)
        val active = actualValues.groupBy { it }.map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        return SigmaResult(sigma, active)
    }

    override fun getCreatedCountOnWeek(
        issueFilter: IssueFilter
    ): List<SimpleAggregatedValue1> {
        val items = pg.getCreatedOnWeekByPartner(issueFilter)
        val l = items.sumBy { it.value }
        var s = 0
        val arr = arrayListOf<SimpleAggregatedValue1>()
        for (item in items) {
            if (s + item.value < l * 0.75 && items.size > 5) {
                arr.add(item)
                s += item.value
            }
        }
        if (s != l) arr.add(SimpleAggregatedValue1(arr.size + 1, "Прочие проекты", l - s))
        return arr
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

    override fun getVelocity(issueFilter: IssueFilter): Any {
        return pg.getVelocity(issueFilter).groupingBy { it.week }.aggregate { _, accumulator: VelocityAggregated?, element, first ->
            if (first) {
                VelocityAggregated(
                    element.week,
                    if (element.type == "Все типы") element.result else 0,
                    if (element.type == "Bug") element.result else 0,
                    if (element.type == "Feature") element.result else 0,
                    if (element.type == "Консультация") element.result else 0
                )
            } else {
                when (element.type) {
                    "Bug" -> accumulator?.bugs = element.result ?: 0
                    "Feature" -> accumulator?.features = element.result ?: 0
                    "Консультация" -> accumulator?.consultations = element.result ?: 0
                    "Все типы" -> accumulator?.all = element.result ?: 0
                }
                accumulator
            }
        }.map { it.value }.takeLast(issueFilter.limit)
    }
}
