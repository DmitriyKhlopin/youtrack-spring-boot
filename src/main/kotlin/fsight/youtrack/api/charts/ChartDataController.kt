package fsight.youtrack.api.charts

import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedRepo
import fsight.youtrack.models.PartnerFilter
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class ChartDataController(
    private val service: IChartData,
    private val timeAccountingExtendedRepo: TimeAccountingExtendedRepo
) {
    @GetMapping("/api/chart/dynamics")
    fun getTimeLineData(
        @RequestParam("projects", required = false) projects: String? = null,
        @RequestParam("dateFrom", required = false) dateFrom: String? = null,
        @RequestParam("dateTo", required = false) dateTo: String? = null
    ) =
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

    @GetMapping("/api/chart/time_accounting_extended_grouped")
    fun getGroupedTimeUnitsByProjectType(
        @RequestParam("projects", required = false) projects: String = "",
        @RequestParam("dateFrom", required = false) dateFrom: String = "",
        @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = timeAccountingExtendedRepo.getGroupedByProjectType(projects, dateFrom, dateTo)



    @GetMapping("/api/chart/processed")
    fun getProcessedDaily(
        @RequestParam("projects", required = false) projects: String = "",
        @RequestParam("dateFrom", required = false) dateFrom: String = "",
        @RequestParam("dateTo", required = false) dateTo: String = ""
    ): Any {
        return service.getProcessedDaily(projects, dateFrom, dateTo)
    }
}
