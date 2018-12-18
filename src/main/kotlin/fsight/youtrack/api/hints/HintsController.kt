package fsight.youtrack.api.hints

import org.springframework.web.bind.annotation.*

@RestController
class HintsController(private val service: IHints) {
    @GetMapping("/api/hints/repositories")
    fun getRepositories(
        @RequestParam("project", required = false) project: String? = null,
        @RequestParam("customer", required = false) customer: String? = null
    ) = when (customer) {
        null -> service.getAllRepositories()
        else -> service.getRepositoriesByCustomer(project, customer)
    }

    @PostMapping("/api/hints/repositories")
    fun postRepository(@RequestBody body: Hints.CustomerRepository) = service.postRepository(body)

}