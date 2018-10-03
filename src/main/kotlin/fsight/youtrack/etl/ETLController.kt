package fsight.youtrack.etl

import fsight.youtrack.scheduler.ScheduledTasks
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000/", "http://10.0.172.42:3000"])
@RestController
class ETLController(private val service: ETLService, private val component: ScheduledTasks) {
    @GetMapping("/etl")
    fun loadData() = if (component.isRunning) null
    else service.loadDataFromYT()

}