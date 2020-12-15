package fsight.youtrack.etl.issues

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IssueController(private val service: IIssue) {
    @GetMapping("/api/etl/issue/search")
    fun search(
        @RequestParam("filter", required = true) filter: String,
        @RequestParam("fields", required = true) fields: String
    ) = service.search(filter, fields.split(","))

    @GetMapping("/api/etl/issue/check")
    fun check(
        @RequestParam("id", required = true) id: String,
        @RequestParam("filter", required = false) filter: String?
    ) = service.checkIfIssueExists(id, filter ?: "")

    @GetMapping("/api/etl/issue/history")
    fun getSingleIssueHistory(
        @RequestParam("id", required = true) id: String
    ) = service.getSingleIssueHistory(id)

    /*@GetMapping("/api/etl/issue/detailed_timeline_to_be_calculated")
    fun getIssuesForDetailedTimelineCalculation() = service.calculateDetailedTimeline()*/

    @GetMapping("/api/etl/issue/detailed_timeline_to_be_calculated")
    fun calculateDetailedTimelineById(@RequestParam("id", required = false) id: String?) = when (id) {
        null -> service.calculateDetailedTimeline()
        else -> service.calculateDetailedTimelineById(id)
    }
}
