package fsight.youtrack.api.issues

import fsight.youtrack.api.reports.ILicensingReport
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class IssuesController(private val service: IIssues, private val service2: ILicensingReport) {
    @GetMapping("/api/issues/high_priority")
    fun getHighPriorityIssuesWithTFSDetails(
        @RequestParam("projects", required = false) projects: String? = null,
        @RequestParam("customers", required = false) customers: String? = null,
        @RequestParam("priorities", required = false) priorities: String? = null,
        @RequestParam("states", required = false) states: String? = null
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(
            service.getHighPriorityIssuesWithDevOpsDetails(
                projects,
                customers,
                priorities,
                states
            )
        )
    }

    @PostMapping("/api/issues/detailed")
    fun getIssuesWithTFSDetails(@RequestBody issueFilter: IssueFilter): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getIssuesWithTFSDetails(issueFilter))
    }


    @PostMapping("/api/issues/sigma")
    fun getIssuesBySigmaValue(
        @RequestParam("days", required = false) days: Int? = null,
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getIssuesBySigmaValue(days ?: 1, issueFilter))
    }

    @GetMapping("/api/issues/stabilization/indicator/1")
    fun getStabilizationIndicator1Issues(
        @RequestParam("year", required = true) year: Int? = null,
        @RequestParam("month", required = true) month: Int? = null,
        @RequestParam("set", required = false) set: Int? = null
    ): ResponseEntity<Any> {
        println(year)
        println(month)
        println(set)
        return ResponseEntity.ok("")
    }

    @GetMapping("/api/issues/detailed_state_transitions")
    fun getIssuesDetailedTimelineById(@RequestParam("issueId", required = true) issueId: String): Any {
        return service.getIssuesDetailedTimelineById(issueId)
    }

    @GetMapping("/api/issues/state_transitions")
    fun getIssuesTimelineById(@RequestParam("issueId", required = true) issueId: String): Any {
        return service.getIssuesTimelineById(issueId)
    }

    @GetMapping("/api/issues/unresolved")
    fun getUnresolvedIssues(): Any {
        return service.getUnresolved()
    }
}
