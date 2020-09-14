package fsight.youtrack.api.projects

import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject

interface IProject {
    fun getProjects(): List<YouTrackProject>
    fun getCommercialProjects(): List<YouTrackProject>
    fun getPartnerCustomers(): List<PartnerCustomerPair>
    fun attachWorkflow(data: ProjectWorkflow): Boolean
    fun attachCustomField(data: String): Any
}
