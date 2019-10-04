package fsight.youtrack.api.status

import com.google.gson.Gson
import fsight.youtrack.models.ServerStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Status {
    @GetMapping("/api/status")
    fun getStatus(): ResponseEntity<Any> = ResponseEntity.ok().body(Gson().toJson(ServerStatus(result = true)))
}
