package fsight.youtrack.api.charts

import fsight.youtrack.models.TimeLine
import org.jooq.DSLContext
import org.jooq.impl.DSL.sum
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youtrack.jooq.tables.Dynamics.DYNAMICS
import java.sql.Timestamp

@Service
@Transactional
class ChartDataImplementation(private val dslContext: DSLContext) : ChartDataService {
    override fun getTimeLineData(projects: Array<String>, weeks: Array<Timestamp>): List<TimeLine> {

        return dslContext.select(
                DYNAMICS.W.`as`("week"),
                sum(DYNAMICS.ACTIVE).`as`("active"),
                sum(DYNAMICS.CREATED).`as`("created"),
                sum(DYNAMICS.RESOLVED).`as`("resolved")
        ).from(DYNAMICS).groupBy(DYNAMICS.W).fetchInto(TimeLine::class.java)
    }
}