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
        /*return ResponseEntity.ok().body(days)*/
    }

    @GetMapping("/api/issues/detailed_state_transitions")
    fun getDetailedStateTransitions(@RequestParam("issueId", required = true) issueId: String): Any {
        return service.getDetailedStateTransitions(issueId)
    }
}
