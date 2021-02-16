package fsight.youtrack.api.integrations.youtrack

import fsight.youtrack.integrations.youtrack.IIssuesAnalyzer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IssuesAnalyzerController {

    @Autowired
    private lateinit var service: IIssuesAnalyzer

    @GetMapping("/api/integrations/youtrack/issues/analyze")
    fun analyze() = service.analyze()
}
