package fsight.youtrack.etl.projects

import fsight.youtrack.models.ProjectModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectsController(private val provider: ProjectsInterface) {
    @GetMapping("/projects")
    fun getProjects(): List<ProjectModel> {
        return provider.getProjects()
    }

}