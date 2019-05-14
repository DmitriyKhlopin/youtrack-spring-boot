package fsight.youtrack.api.projects

import fsight.youtrack.generated.jooq.tables.PartnerCustomers.PARTNER_CUSTOMERS
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.YouTrackProject
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Project(private val dslContext: DSLContext) : IProject {
    override fun getProjects(): List<YouTrackProject> {
        return dslContext.select(
            PROJECTS.NAME.`as`("name"),
            PROJECTS.SHORT_NAME.`as`("shortName")
        ).from(PROJECTS).fetchInto(YouTrackProject::class.java)
    }

    data class PartnerCustomerPair(
        var project: String? = null,
        var customer: String? = null
    )


    override fun getPartnerCustomers(): List<PartnerCustomerPair> {
        return dslContext.select(
            PARTNER_CUSTOMERS.FIELD_VALUE.`as`("customer"),
            PARTNER_CUSTOMERS.PROJECT_SHORT_NAME.`as`("project")
        ).from(PARTNER_CUSTOMERS).fetchInto(PartnerCustomerPair::class.java)
    }
}
