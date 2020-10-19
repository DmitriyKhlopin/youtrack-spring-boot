package fsight.youtrack.api.projects

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@CrossOrigin
@RestController
class ProjectController(private val service: IProject) {
    @Deprecated("Use /dictionaries/project")
    @GetMapping("/api/project")
    fun getProjects() = service.getProjects()



    @Deprecated("Use /dictionaries/partner_customers")
    @GetMapping("/api/partner_customers")
    fun getPartnerCustomers() = service.getPartnerCustomers()


    @PostMapping("/api/projects/workflows")
    fun attachWorkflow(@RequestBody data: ProjectWorkflow): ResponseEntity<Any> {
        return ResponseEntity.ok(service.attachWorkflow(data))
    }

    @PostMapping("/api/projects/custom_field/attach")
    fun attachCustomField(@RequestBody data: String): ResponseEntity<Any> {
        return ResponseEntity.ok(service.attachCustomField(data))
    }
}


