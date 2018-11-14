package fsight.youtrack.api.time

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class TimeAccountingController(private val service: TimeAccountingService) {
    @GetMapping("/api/wi_today")
    fun getTimeAccountingToday() = service.getWorkItemsToday("")

    @GetMapping("/api/wi_yesterday")
    fun getTimeAccountingYesterday() = service.getWorkItemsYesterday("")

    @GetMapping("/api/time")
    fun getWorkItems(@RequestParam("dateFrom", required = false) dateFrom: String? = null,
                     @RequestParam("dateTo", required = false) dateTo: String? = null) = service.getWorkItems(dateFrom, dateTo)
}