package fsight.youtrack.api.users

import com.google.gson.Gson
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class UsersController(private val service: Users) {
    @GetMapping("/api/users")
    fun getAll(): String = Gson().toJson(service.getAll())
}