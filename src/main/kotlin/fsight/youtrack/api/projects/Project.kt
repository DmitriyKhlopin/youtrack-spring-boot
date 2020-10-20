package fsight.youtrack.api.projects

import fsight.youtrack.AUTH
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.common.IResolver
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.CustomFieldPresets.CUSTOM_FIELD_PRESETS
import fsight.youtrack.generated.jooq.tables.PartnerCustomers.PARTNER_CUSTOMERS
import fsight.youtrack.generated.jooq.tables.ProjectType.PROJECT_TYPE
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.PartnerCustomerPair
import fsight.youtrack.models.YouTrackProject
import fsight.youtrack.models.web.ReactSelectOption
import fsight.youtrack.models.youtrack.CustomField
import fsight.youtrack.models.youtrack.isValid
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Project(private val dsl: DSLContext) : IProject {
    @Autowired
    lateinit var resolver: IResolver

    @Autowired
    private lateinit var pg: IPGProvider

    override fun getProjects(): List<YouTrackProject> {
        return dsl.select(
            PROJECTS.NAME.`as`("name"),
            PROJECTS.SHORT_NAME.`as`("shortName"),
            PROJECTS.ID.`as`("id")
        ).from(PROJECTS).fetchInto(YouTrackProject::class.java)
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

    override fun attachCustomField(data: String): Any {
        println(data)
        val field = dsl
            .select(
                CUSTOM_FIELD_PRESETS.NAME.`as`("name"),
                CUSTOM_FIELD_PRESETS.ID.`as`("id"),
                CUSTOM_FIELD_PRESETS.VALUE_TYPE.`as`("valueType")
            )
            .from(CUSTOM_FIELD_PRESETS)
            .where(CUSTOM_FIELD_PRESETS.NAME.eq(data))
            .fetchOneInto(CustomField::class.java)
        if (!field.isValid()) return "You have failed"

        /*return this.getCommercialProjects()*/
        val ids = pg.getCommercialProjects().mapNotNull { it.id }/*.filter { it == "0-103" }*/
        ids.forEach {
            val body = "{\"field\": {\"id\":\"${field.id}\",\"\$type\":\"CustomField\"},\"\$type\":\"${resolver.valueTypeToYouTrackProjectFieldType(field.valueType ?: "")}\"}"
            println(body)
            val response = YouTrackAPI.create().attachCustomField(AUTH, it, body).execute()
            println(response.body())
        }
        return ""
    }
}

