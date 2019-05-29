package fsight.youtrack.api.issues

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class IssuesController(private val service: IIssues) {
    @GetMapping("/api/issues/high_priority")
    fun getHighPriorityIssuesWithTFSDetails(
        @RequestParam(
            "projects",
            required = false
        ) projects: String? = null,
        @RequestParam(
            "customers",
            required = false
        ) customers: String? = null,
        @RequestParam(
            "priorities",
            required = false
        ) priorities: String? = null,
        @RequestParam(
            "states",
            required = false
        ) states: String? = null
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getHighPriorityIssuesWithTFSDetails(projects, customers, priorities, states))
    }
}
