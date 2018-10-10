package fsight.youtrack.api.projects

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000/", "http://10.0.172.42:3000"])
@RestController
class ProjectController(private val service: ProjectService) {
    @GetMapping("/api/project")
    fun getProjects() = service.getProjects()
}