package fsight.youtrack.api.integrations.revisions

import fsight.youtrack.integrations.devops.revisions.IDevOpsRevisions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RevisionsController {
    @Autowired
    private lateinit var service: IDevOpsRevisions

    @GetMapping("/api/integrations/devops/revisions/start")
    fun startRevision(
        @RequestParam("stage", required = false) stage: Int? = null,
        @RequestParam("limit", required = false) limit: Int? = null
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(
            service.getActiveBugsAndFeatures(
                stage,
                limit,
                null
            )
        )
    }
}
