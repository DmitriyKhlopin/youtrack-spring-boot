package fsight.youtrack.api.etl

import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class ETLController(private val service: IETL) {
    @GetMapping("/api/etl")
    fun loadData(
        @RequestParam("dateFrom", required = false) dateFrom: String? = "",
        @RequestParam("dateTo", required = false) dateTo: String? = ""
    ) =
        if (dateFrom != null && dateTo != null)
            service.loadDataFromYT(true, "updated: $dateFrom .. $dateTo") else
            service.loadDataFromYT(true)

    @GetMapping("/api/etl/state")
    fun getCurrentState() = ETL.etlState

    @GetMapping("/api/etl/bundle")
    fun getBundles() = service.getBundles()

    @GetMapping("/api/etl/users")
    fun getUsers() = service.getUsers()

    @GetMapping("/api/etl/issues/{id}")
    fun getIssueById(@PathVariable("id") id: String) = service.getIssueById(id)
}

