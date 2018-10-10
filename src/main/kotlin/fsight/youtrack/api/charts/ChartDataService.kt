package fsight.youtrack.api.charts

import fsight.youtrack.models.SigmaItem
import fsight.youtrack.models.TimeLine

interface ChartDataService {
    fun getTimeLineData(projects: String, dateFrom: String, dateTo: String): List<TimeLine>
    fun getSigmaData(projects: String, dateFrom: String, dateTo: String): List<SigmaItem>
}