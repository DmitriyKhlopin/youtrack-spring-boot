package fsight.youtrack.api.projects

import fsight.youtrack.AUTH
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.generated.jooq.tables.PartnerCustomers.PARTNER_CUSTOMERS
import fsight.youtrack.generated.jooq.tables.ProjectType.PROJECT_TYPE
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Project(private val dsl: DSLContext) : IProject {
    override fun getProjects(): List<YouTrackProject> {
        return dsl.select(
            PROJECTS.NAME.`as`("name"),
            PROJECTS.SHORT_NAME.`as`("shortName")
        ).from(PROJECTS).fetchInto(YouTrackProject::class.java)
    }


    override fun getPartnerCustomers(): List<PartnerCustomerPair> {
        return dsl.select(
            PARTNER_CUSTOMERS.FIELD_VALUE.`as`("customer"),
            PARTNER_CUSTOMERS.PROJECT_SHORT_NAME.`as`("project")
        ).from(PARTNER_CUSTOMERS).fetchInto(PartnerCustomerPair::class.java)
    }

    override fun attachWorkflow(data: ProjectWorkflow): Boolean {
        println(data)
        val ids = dsl.select(PROJECTS.ID)
            .from(PROJECTS)
            .leftJoin(PROJECT_TYPE).on(PROJECTS.SHORT_NAME.eq(PROJECT_TYPE.PROJECT_SHORT_NAME))
            .where(PROJECT_TYPE.IS_PUBLIC.eq(true))
            .or(PROJECT_TYPE.IS_PUBLIC.isNull)
            .fetchInto(String::class.java)
        println(ids)

        data.workflowId ?: return false
        ids.forEach {
            val response = YouTrackAPI.create().attachWorkflow(AUTH, it, "{\"workflow\": {\"id\": \"${data.workflowId}\"}}").execute()
            println(response.body())
        }
        return true
    }
}

