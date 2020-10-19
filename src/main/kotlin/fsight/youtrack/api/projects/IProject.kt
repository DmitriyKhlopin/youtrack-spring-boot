package fsight.youtrack.api.projects

import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject
import fsight.youtrack.models.web.ReactSelectOption

interface IProject {
    fun getProjects(): List<YouTrackProject>
    fun getPartnerCustomers(): List<PartnerCustomerPair>
    fun attachWorkflow(data: ProjectWorkflow): Boolean
    fun attachCustomField(data: String): Any
}
