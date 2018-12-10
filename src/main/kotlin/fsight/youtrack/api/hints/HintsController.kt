package fsight.youtrack.api.hints

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HintsController(private val service: IHints) {
    @GetMapping("/api/hints/repositories")
    fun getTimeLineData(
        @RequestParam("projects", required = false) project: String? = null,
        @RequestParam("customer", required = false) customer: String? = null
    ) = service.getNewIssueHints(project, customer)
}