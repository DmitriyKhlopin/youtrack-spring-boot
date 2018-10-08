package fsight.youtrack.etl.projects

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class ProjectsController(private val provider: ProjectsInterface) {

    @GetMapping("/projects")
    fun getProjects(): ModelAndView {
        val items = provider.getProjects()
        /*model.addAttribute(" items", items)*/
        return ModelAndView("projects", "items", items)
    }

}