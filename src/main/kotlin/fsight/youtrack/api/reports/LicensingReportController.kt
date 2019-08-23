package fsight.youtrack.api.reports

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class LicensingReportController(private val service: ILicensingReport) {
    @GetMapping("/api/lic")
    fun getData(): ResponseEntity<Any> = ResponseEntity.ok(service.getData())
}
