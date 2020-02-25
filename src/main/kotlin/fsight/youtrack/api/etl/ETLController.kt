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
            @RequestParam("dateFrom", required = false) dateFrom: String? = "",
            @RequestParam("dateTo", required = false) dateTo: String? = "",
            @RequestParam("parameters", required = false) parameters: String = ""
    ) =
            if (dateFrom != null && dateTo != null)
                service.loadDataFromYT(
                        manual = true,
                        customFilter = "updated: $dateFrom .. $dateTo",
                        parameters = parameters
                ) else
                service.loadDataFromYT(manual = true, parameters = parameters)

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
}
