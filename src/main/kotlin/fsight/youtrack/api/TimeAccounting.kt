package fsight.youtrack.api

import fsight.youtrack.dashboard.DashboardService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000/", "http://10.0.172.42:3000"])
@RestController
class TimeAccounting(private val dashboardService: DashboardService) {
    @GetMapping("/api/wi_today")
    fun getTimeAccountingToday() = dashboardService.getWorkItemsToday("")

    @GetMapping("/api/wi_yesterday")
    fun getTimeAccountingYesterday() = dashboardService.getWorkItemsYesterday("")
}