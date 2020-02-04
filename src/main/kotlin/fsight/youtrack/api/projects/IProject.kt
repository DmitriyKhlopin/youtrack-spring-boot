package fsight.youtrack.api.projects

import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject

interface IProject {
    fun getProjects(): List<YouTrackProject>
    fun getPartnerCustomers(): List<PartnerCustomerPair>
}
