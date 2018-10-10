package fsight.youtrack.api.tfs

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000", "http://10.0.172.42:3000"])
@RestController
class TFSDataController(private val service: TFSDataService) {
    @GetMapping("/api/tfs")
    fun getRequirements() = service.getRequirements()
}