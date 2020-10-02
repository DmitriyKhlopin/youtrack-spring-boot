package fsight.youtrack.api.etl


import fsight.youtrack.etl.IETL
import fsight.youtrack.etl.IETLState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class ETLController {
    @Autowired
    lateinit var service: IETL

    @Autowired
    lateinit var etlStateService: IETLState



    @GetMapping("/api/etl")
    fun loadData(
        @RequestParam("dateType", required = false) dateType: String? = "",
        @RequestParam("dateFrom", required = false) dateFrom: String? = "",
        @RequestParam("dateTo", required = false) dateTo: String? = "",
        @RequestParam("parameters", required = false) parameters: String = ""
    ) =
        if (dateFrom != null && dateTo != null)
            service.runManualExport(
                customFilter = "$dateType: $dateFrom .. $dateTo",
                parameters = parameters,
                dateFrom = dateFrom,
                dateTo = dateTo
            ) else
            service.runManualExport(parameters = parameters)

    @GetMapping("/api/etl/state")
    fun getCurrentState() = etlStateService.state

    @GetMapping("/api/etl/bundle")
    fun getBundles() = service.getBundles()

    @GetMapping("/api/etl/users")
    fun getUsers() = service.getUsers()

    @GetMapping("/api/etl/issues/{id}")
    fun getIssueById(@PathVariable("id") id: String) = service.getIssueById(id)

    @GetMapping("/api/etl/history/{id}")
    fun getHistory(@PathVariable("id") id: String) {
        service.getIssueHistory(id)
    }

    @GetMapping("/api/etl/timeline/{id}")
    fun getTimeline(@PathVariable("id") id: String): Any {
        return service.getTimelineById(id)
    }

    @GetMapping("/api/etl/timeline/period")
    fun launchCalculationForPeriod(): Any {
        return service.launchCalculationForPeriod()
    }
}
