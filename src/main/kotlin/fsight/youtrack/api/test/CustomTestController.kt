package fsight.youtrack.api.test

import fsight.youtrack.gkbQueriesFull
import fsight.youtrack.gkbQueriesShort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomTestController(private val service: ICustomTest) {
    @GetMapping("/api/test/increment")
    fun increment() = service.increment()

    @GetMapping("/api/test/gkb")
    fun gkbTest(@RequestParam("option", required = false) option: Int? = null) = when (option) {
        1 -> service.repositoryEntrance(gkbQueriesFull)
        2 -> service.repositoryEntrance(gkbQueriesShort)
        else -> service.repositoryEntrance(gkbQueriesShort)
    }
}