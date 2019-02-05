package fsight.youtrack.etl.projects

import fsight.youtrack.models.YouTrackProject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectsController(private val provider: IProjects) {
    @GetMapping("/projects")
    fun getProjects(): List<YouTrackProject> {
        return provider.getProjects()
    }
}
