package fsight.youtrack.db.exposed

import fsight.youtrack.db.exposed.ms.ScheduleTimeIntervalRepo
import fsight.youtrack.db.exposed.pg.ProjectDictionaryRepo
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class TestController(private val schedule: ScheduleTimeIntervalRepo, private val project: ProjectDictionaryRepo) {
    @GetMapping("/api/project_test")
    fun getProjects() = project.get()

    @GetMapping("/api/schedule_test")
    fun getSchedules() = schedule.getTopTen()
}