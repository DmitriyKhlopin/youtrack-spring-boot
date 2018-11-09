package fsight.youtrack.api.charts

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class ChartDataController(private val service: ChartDataService) {
    @GetMapping("/api/chart/dynamics")
    fun getTimeLineData(@RequestParam("projects", required = false) projects: String? = null,
                        @RequestParam("dateFrom", required = false) dateFrom: String? = null,
                        @RequestParam("dateTo", required = false) dateTo: String? = null) =
            if (projects != null && dateFrom != null && dateTo != null) service.getTimeLineData(projects, dateFrom, dateTo)
            else service.getTimeLineData()

    @GetMapping("/api/chart/sigma")
    fun getSigmaData(
            @RequestParam("projects", required = false) projects: String = "",
            @RequestParam("dateFrom", required = false) dateFrom: String = "",
            @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = service.getSigmaData(projects, dateFrom, dateTo)

    @GetMapping("/api/chart/created_on_week")
    fun getCreatedCountOnWeek(
            @RequestParam("projects", required = false) projects: String = "",
            @RequestParam("dateFrom", required = false) dateFrom: String = "",
            @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = service.getCreatedCountOnWeek(projects, dateFrom, dateTo)

    @GetMapping("api/chart/gantt")
    fun getGanttData() = service.getGanttData()
}
