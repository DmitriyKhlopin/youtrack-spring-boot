package fsight.youtrack.api.test


import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomTestController(private val service: ICustomTest) {
    @GetMapping("/api/test/increment")
    fun increment() = service.increment()
}