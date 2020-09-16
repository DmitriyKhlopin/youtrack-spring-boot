package fsight.youtrack.api.reports

import fsight.youtrack.models.PartnerFilter
import org.springframework.web.bind.annotation.*

@RestController
class ReportController(private val service: IReport) {
    @PostMapping("/api/report/partner/state")
    fun getStatesByPartner(@RequestBody filters: List<PartnerFilter>): Any {
        return service.getStatesByPartner(filters)
    }

    @PostMapping("/api/report/partner/type")
    fun getTypesByPartner(@RequestBody filters: List<PartnerFilter>): Any {
        return service.getTypesByPartner(filters)
    }

    @PostMapping("/api/report/partner/priority")
    fun getPrioritiesByPartner(@RequestBody filters: List<PartnerFilter>): Any {
        return service.getPrioritiesByPartner(filters)
    }

    @PostMapping("/api/report/partner/customer")
    fun getCustomersByPartner(@RequestBody filters: List<PartnerFilter>): Any {
        return service.getCustomersByPartner(filters)
    }

    @PostMapping("/api/report/partner/product")
    fun getProductsByPartner(@RequestBody filters: List<PartnerFilter>): Any {
        return service.getProductsByPartner(filters)
    }
}

