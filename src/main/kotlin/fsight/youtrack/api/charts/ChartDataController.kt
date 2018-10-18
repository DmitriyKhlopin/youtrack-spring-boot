package fsight.youtrack.api.charts

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000", "http://10.0.172.42:3000"])
@RestController
class ChartDataController(private val service: ChartDataService) {
    @GetMapping("/api/chart/dynamics")
    fun getTimeLineData(
            @RequestParam("projects", required = false) projects: String = "",
            @RequestParam("dateFrom", required = false) dateFrom: String = "",
            @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = service.getTimeLineData(projects, dateFrom, dateTo)

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

}
