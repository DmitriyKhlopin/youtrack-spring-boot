package fsight.youtrack.api.charts

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000/", "http://10.0.172.42:3000"])
@RestController
class ChartDataController(private val service: ChartDataService) {
    @GetMapping("/api/chart/1")
    fun getTimeLineData() = service.getTimeLineData(arrayOf(), arrayOf())
}
