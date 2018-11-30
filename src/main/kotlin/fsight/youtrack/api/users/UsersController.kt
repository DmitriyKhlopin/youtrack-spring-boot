package fsight.youtrack.api.users

import com.google.gson.Gson
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class UsersController(private val service: Users) {
    @GetMapping("/api/users")
    fun getAll(): ResponseEntity<Any> {
        val headers = HttpHeaders()
        headers.add("Content-Type", "application/json; charset=UTF-8")
        return ResponseEntity(Gson().toJson(service.getAll()), HttpStatus.OK)
    }
}