package fsight.youtrack.api.dictionaries

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DictionaryController(private val service: IDictionary) {

    @GetMapping("/api/dictionary/devOpsStates")
    fun getDevOpsStates() = service.devOpsStates

    @GetMapping("/api/dictionary/tags")
    fun getTags() = service.getTags()

    @GetMapping("/api/dictionary/partner_customers")
    fun getPartnerCustomers() = service.getPartnerCustomers()

    @GetMapping("/api/dictionary/project/commercial")
    fun getCommercialProjects() = service.commercialProjects
}
