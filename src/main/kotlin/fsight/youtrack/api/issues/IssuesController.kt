package fsight.youtrack.api.issues

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class IssuesController(private val service: IIssues) {
    @GetMapping("/api/issues/high_priority")
    fun getHighPriorityIssuesWithTFSDetails(): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getHighPriorityIssuesWithTFSDetails())
    }
}
