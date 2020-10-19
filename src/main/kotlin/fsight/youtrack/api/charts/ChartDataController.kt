package fsight.youtrack.api.charts

import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class ChartDataController(

    private val timeAccountingExtendedRepo: TimeAccountingExtendedRepo
) {
    @Autowired
    lateinit var service: ChartData

    @GetMapping("/api/chart/dynamics")
    fun getTimeLineData(
        @RequestParam("projects", required = false) projects: String? = null,
        @RequestParam("dateFrom", required = false) dateFrom: String? = null,
        @RequestParam("dateTo", required = false) dateTo: String? = null
    ) =
        service.getDynamicsData(projects, dateFrom, dateTo)

    @GetMapping("/api/chart/sigma")
    fun getSigmaData(
        @RequestParam("projects", required = false) projects: String = "",
        @RequestParam("types", required = false) types: String = "",
        @RequestParam("states", required = false) states: String = "",
        @RequestParam("dateFrom", required = false) dateFrom: String = "",
        @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = service.getSigmaData(projects, types, states, dateFrom, dateTo)

    @GetMapping("/api/chart/created_on_week")
    fun getCreatedCountOnWeek(
        @RequestParam("projects", required = false) projects: String = "",
        @RequestParam("dateFrom", required = false) dateFrom: String = "",
        @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = service.getCreatedCountOnWeek(projects, dateFrom, dateTo)

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
