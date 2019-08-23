package fsight.youtrack.api.charts

import fsight.youtrack.models.SigmaResult
import fsight.youtrack.models.TimeLine
import org.springframework.http.ResponseEntity

interface IChartData {
    fun getTimeLineData(projects: String, dateFrom: String, dateTo: String): List<TimeLine>
    fun getTimeLineData(): List<TimeLine>
    fun getSigmaData(projects: String, dateFrom: String, dateTo: String): SigmaResult
    fun getCreatedCountOnWeek(
        projects: String,
        dateFrom: String,
        dateTo: String
    ): List<ChartData.SimpleAggregatedValue>?

    fun getGanttData(): ResponseEntity<Any>
}
