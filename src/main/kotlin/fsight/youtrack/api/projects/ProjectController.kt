package fsight.youtrack.api.projects

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@CrossOrigin
@RestController
class ProjectController(private val service: IProject) {
    @Deprecated("Use /dictionaries/project")
    @GetMapping("/api/project")
    fun getProjects() = service.getProjects()

    @Deprecated("Use /dictionaries/partner_customers")
    @GetMapping("/api/partner_customers")
    fun getPartnerCustomers() = service.getPartnerCustomers()
}
