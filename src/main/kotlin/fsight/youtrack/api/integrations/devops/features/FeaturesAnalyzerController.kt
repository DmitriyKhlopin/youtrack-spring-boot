package fsight.youtrack.api.integrations.devops.features

import fsight.youtrack.integrations.devops.features.IFeaturesAnalyzer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FeaturesAnalyzerController {
    @Autowired
    private lateinit var f: IFeaturesAnalyzer

    @GetMapping("/api/integrations/devops/features/analyze")
    fun analyze(): ResponseEntity<Any> {
        return ResponseEntity.ok(
            f.analyze()
        )
    }
}
