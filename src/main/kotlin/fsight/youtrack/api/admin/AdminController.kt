package fsight.youtrack.api.admin

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.sql.Timestamp


@RestController
@CrossOrigin
class AdminController(private val service: IAdmin) {
    data class Build(var build: String, var date: Timestamp)

    @PostMapping("/api/admin/build")
    fun postBuild(@RequestBody body: Build): ResponseEntity<Any> {
        return ResponseEntity.ok().body(service.addBuild(body.build, body.date))
    }
}
