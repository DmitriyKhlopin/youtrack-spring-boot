package fsight.youtrack.api.charts

import fsight.youtrack.models.TimeLine

interface ChartDataService {
    fun getTimeLineData(projects: String, dateFrom: String, dateTo: String): List<TimeLine>
}