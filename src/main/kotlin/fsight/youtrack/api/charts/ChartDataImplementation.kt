package fsight.youtrack.api.charts

import fsight.youtrack.generated.jooq.tables.Dynamics.DYNAMICS
import fsight.youtrack.models.TimeLine
import org.jooq.DSLContext
import org.jooq.impl.DSL.sum
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
}