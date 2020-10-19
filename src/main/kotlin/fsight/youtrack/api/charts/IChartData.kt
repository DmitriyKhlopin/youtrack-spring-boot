package fsight.youtrack.api.charts

import fsight.youtrack.models.Dynamics
import fsight.youtrack.models.SigmaResult
import org.springframework.http.ResponseEntity

interface IChartData {
    fun getDynamicsData(projects: String?, dateFrom: String?, dateTo: String?): List<Dynamics>
    fun getSigmaData(projects: String, types: String, states: String, dateFrom: String, dateTo: String): SigmaResult
    fun getCreatedCountOnWeek(projects: String, dateFrom: String, dateTo: String): List<ChartData.SimpleAggregatedValue>?
    fun getProcessedDaily(projects: String, dateFrom: String, dateTo: String): Any
}
