package fsight.youtrack.etl

import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000", "http://10.0.172.42:3000"])
@RestController
class ETLController(private val service: ETLService) {
    @GetMapping("/etl")
    fun loadData(@RequestParam("dateFrom", required = false) dateFrom: String = "",
                 @RequestParam("dateTo", required = false) dateTo: String = "") =
            service.loadDataFromYT(true, "updated: $dateFrom .. $dateTo")

    @GetMapping("/etl/state")
    fun getCurrentState() = service.getCurrentState()

    @GetMapping("/etl/bundle")
    fun getBundles() = service.getBundles()

    @GetMapping("/etl/issues/{id}")
    fun getIssueById(@PathVariable("id") id: String) = service.getIssueById(id)
}