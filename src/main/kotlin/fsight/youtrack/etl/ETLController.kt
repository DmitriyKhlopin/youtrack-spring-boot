package fsight.youtrack.etl

import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class ETLController(private val service: IETL) {
    @GetMapping("/api/etl")
    fun loadData(
        @RequestParam("dateFrom", required = false) dateFrom: String? = "",
        @RequestParam("dateTo", required = false) dateTo: String? = "",
        @RequestParam("complete", required = false) complete: Boolean = false
    ) =
        if (dateFrom != null && dateTo != null)
            service.loadDataFromYT(
                manual = true,
                customFilter = "updated: $dateFrom .. $dateTo",
                complete = complete
            ) else
            service.loadDataFromYT(manual = true, complete = complete)

    @GetMapping("/api/etl/state")
    fun getCurrentState() = ETL.etlState

    @GetMapping("/api/etl/bundle")
    fun getBundles() = service.getBundles()

    @GetMapping("/api/etl/users")
    fun getUsers() = service.getUsers()

    @GetMapping("/api/etl/issues/{id}")
    fun getIssueById(@PathVariable("id") id: String) = service.getIssueById(id)
}

