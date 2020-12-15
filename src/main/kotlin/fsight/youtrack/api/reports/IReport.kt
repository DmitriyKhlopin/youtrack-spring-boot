package fsight.youtrack.api.reports

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.models.PartnerFilter

interface IReport {
    fun getStatesByPartner(filters: List<PartnerFilter>): Any
    fun getTypesByPartner(filters: List<PartnerFilter>): Any
    fun getPrioritiesByPartner(filters: List<PartnerFilter>): Any
    fun getCustomersByPartner(filters: List<PartnerFilter>): Any
    fun getProductsByPartner(filters: List<PartnerFilter>): Any
}
