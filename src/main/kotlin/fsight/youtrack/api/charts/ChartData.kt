package fsight.youtrack.api.charts

import com.google.gson.GsonBuilder
import fsight.youtrack.*
import fsight.youtrack.generated.jooq.tables.Dynamics.DYNAMICS
import fsight.youtrack.generated.jooq.tables.DynamicsProcessedByDay
import fsight.youtrack.generated.jooq.tables.DynamicsProcessedByDay.DYNAMICS_PROCESSED_BY_DAY
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.models.*
import fsight.youtrack.models.sql.ValueByDate
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.sum
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import kotlin.math.sqrt

@Service
class ChartData(private val dslContext: DSLContext) : IChartData {

    data class SimpleAggregatedValue(val name: String, val value: Int)

    override fun getTimeLineData(projects: String, dateFrom: String, dateTo: String): List<TimeLine> {
        return dslContext.select(
            DYNAMICS.W.`as`("week"),
            sum(DYNAMICS.ACTIVE).`as`("active"),
            sum(DYNAMICS.CREATED).`as`("created"),
            sum(DYNAMICS.RESOLVED).`as`("resolved")
        )
            .from(DYNAMICS)
            .where(
                DYNAMICS.W.between(dateFrom.toStartOfWeek(), dateTo.toStartOfWeek())
            )
            .and(DYNAMICS.SHORT_NAME.`in`(projects.splitToList()))
            .groupBy(DYNAMICS.W)
            .fetchInto(TimeLine::class.java)
    }

    override fun getTimeLineData(): List<TimeLine> {
        return dslContext.select(
            DYNAMICS.W.`as`("week"),
            sum(DYNAMICS.ACTIVE).`as`("active"),
            sum(DYNAMICS.CREATED).`as`("created"),
            sum(DYNAMICS.RESOLVED).`as`("resolved")
        )
            .from(DYNAMICS)
            .groupBy(DYNAMICS.W)
            .fetchInto(TimeLine::class.java)
    }

    override fun getSigmaData(projects: String, dateFrom: String, dateTo: String): SigmaResult {
        val filter = projects.splitToList()
        val items: List<Int> =
            dslContext.select(DSL.coalesce(ISSUES.TIME_AGENT, 0) + DSL.coalesce(ISSUES.TIME_DEVELOPER, 0))
                .from(ISSUES)
                .where(ISSUES.RESOLVED_DATE.lessOrEqual(dateTo.toStartOfDate()))
                .and(ISSUES.RESOLVED_DATE.isNotNull)
                /*.and(ISSUES.ISSUE_TYPE.eq("Feature"))*/
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
        val active = dslContext.select(DSL.coalesce(ISSUES.TIME_AGENT, 0) + DSL.coalesce(ISSUES.TIME_DEVELOPER, 0))
            .from(ISSUES)
            .where(ISSUES.CREATED_DATE.lessOrEqual(dateTo.toStartOfDate()))
            .and(ISSUES.RESOLVED_DATE.isNull)
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(filter))
            .orderBy(ISSUES.CREATED_DATE.desc())
            .fetchInto(Int::class.java).groupBy { 1 + it / 32400 }
            .map { item -> SigmaItem(item.key, item.value.size) }.sortedBy { it.day }.toList()
        return SigmaResult(sigma, active)
    }

    override fun getCreatedCountOnWeek(
        projects: String,
        dateFrom: String,
        dateTo: String
    ): List<SimpleAggregatedValue> {
        val filter = projects.splitToList()
        val dt = dateTo.toStartOfWeek()
        println(dateTo.toStartOfWeek())
        return dslContext
            .select(
                ISSUES.PROJECT_SHORT_NAME.`as`("name"),
                DSL.count(ISSUES.PROJECT_SHORT_NAME).`as`("value")
            )
            .from(ISSUES)
            .where(ISSUES.CREATED_WEEK.eq(dt))
            .and(ISSUES.PROJECT_SHORT_NAME.`in`(filter))
            .groupBy(ISSUES.PROJECT_SHORT_NAME)
            .fetch()
            .map { SimpleAggregatedValue(it["name"].toString(), it["value"].toString().toInt()) }
            .sortedByDescending { it.value }
    }

    override fun getGanttData(): ResponseEntity<Any> {
        val i = GetGanttDataRetrofitService.create().get(AUTH).execute().body()
        i?.data?.tasks?.forEach { println(it) }
        return ResponseEntity.status(HttpStatus.OK).body("Here is some data for you")
    }

    data class Report(val data: ReportData? = null, val `$type`: String? = null)
    data class ReportData(val tasks: ArrayList<Task>? = null, val id: String? = null, val `$type`: String? = null)
    data class Task(val id: String? = null, val idealStart: Long? = null, val `$type`: String? = null)

    interface GetGanttDataRetrofitService {
        @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
        @GET("reports/151-13?\$top=-1&fields=data(\$type,id,remainingEffortPresentation,tasks(id,idealStart))")
        fun get(
            @Header("Authorization") auth: String
        ): Call<Report>

        companion object Factory {
            fun create(): GetGanttDataRetrofitService {
                val gson = GsonBuilder().setLenient().create()
                val retrofit =
                    Retrofit.Builder().baseUrl(NEW_ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                return retrofit.create(GetGanttDataRetrofitService::class.java)
            }
        }
    }


    override fun getProcessedDaily(
        projects: String,
        dateFrom: String,
        dateTo: String
    ): Any {
        return dslContext.select(
            DYNAMICS_PROCESSED_BY_DAY.D.`as`("date"),
            DYNAMICS_PROCESSED_BY_DAY.COUNT.`as`("value")
        )
            .from(DYNAMICS_PROCESSED_BY_DAY).fetchInto(ValueByDate::class.java)
    }
}
