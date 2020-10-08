package fsight.youtrack.api.test


import fsight.youtrack.common.IResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomTestController(private val service: ICustomTest) {
    @Autowired
    private lateinit var resolver: IResolver

    @GetMapping("/api/test/increment")
    fun increment() = service.increment()

    @GetMapping("/api/test/resolve")
    fun resolveAreaToTeam(
        @RequestParam("area", required = true) area: String
    ) = resolver.resolveAreaToTeam(area)
}
