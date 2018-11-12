package fsight.youtrack.api.time

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class TimeAccountingController(private val timeAccountingService: TimeAccountingService) {
    @GetMapping("/api/wi_today")
    fun getTimeAccountingToday() = timeAccountingService.getWorkItemsToday("")

    @GetMapping("/api/wi_yesterday")
    fun getTimeAccountingYesterday() = timeAccountingService.getWorkItemsYesterday("")
}