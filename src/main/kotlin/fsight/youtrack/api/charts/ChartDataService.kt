package fsight.youtrack.api.charts

import fsight.youtrack.models.TimeLine
import java.sql.Timestamp

interface ChartDataService {
    fun getTimeLineData(projects: Array<String>, weeks: Array<Timestamp>): List<TimeLine>
}