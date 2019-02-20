package fsight.youtrack.api.durations

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class WorkDurationController(private val service: IWorkDuration) {
    @GetMapping("/api/work_duration")
    fun getByProjectShortName(@RequestParam(name = "projectShortName") projectShortName: String? = null): ResponseEntity<Any> {
        if (projectShortName == null) return ResponseEntity.badRequest().body("Project short name to specified.")
        return ResponseEntity.ok().body(service.getByProject(projectShortName))
    }
}
