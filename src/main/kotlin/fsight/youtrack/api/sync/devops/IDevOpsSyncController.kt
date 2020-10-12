package fsight.youtrack.api.sync.devops

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IDevOpsSyncController {
    @Autowired
    private lateinit var service: IDevOpsSync

    @GetMapping("/api/sync/devops/syncUnresolved")
    fun syncUnresolved() = service.syncUnresolved()
}
