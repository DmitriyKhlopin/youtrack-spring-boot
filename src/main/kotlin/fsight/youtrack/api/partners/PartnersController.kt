package fsight.youtrack.api.partners

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class PartnersController(private val service: IPartners) {
    @GetMapping("/api/partners")
    fun getPartners() = service.getPartners()
}
