package fsight.youtrack.api.priority

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class PriorityManagerController(private val service: IPriorityManager) {
    @PostMapping("/api/priority/manager")
    fun getPriority() = service.getPriority(listOf())
}