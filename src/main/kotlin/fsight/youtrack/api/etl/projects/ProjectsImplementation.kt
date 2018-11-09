package fsight.youtrack.api.etl.projects

import fsight.youtrack.AUTH
import fsight.youtrack.ROOT_REF
import fsight.youtrack.generated.jooq.tables.ProjectCustomFields.PROJECT_CUSTOM_FIELDS
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.ProjectModel
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.SocketTimeoutException

@Service
@Transactional
class ProjectsImplementation(private val dslContext: DSLContext) : ProjectsService {
    override fun getProjects(): List<ProjectModel> {
        return dslContext.select(
                PROJECTS.NAME.`as`("name"),
                PROJECTS.SHORT_NAME.`as`("shortName"),
                PROJECTS.SHORT_NAME.`as`("description"))
                .from(PROJECTS)
                .fetchInto(ProjectModel::class.java)
    }

    override fun saveProjects(): Int {
        var result = 0
        try {
            ProjectsRetrofitService.create().getProjectsList(AUTH).execute().body()?.forEach {
                result = dslContext
                        .insertInto(PROJECTS)
                        .set(PROJECTS.NAME, it.name)
                        .set(PROJECTS.SHORT_NAME, it.shortName)
                        .set(PROJECTS.DESCRIPTION, it.description)
                        .onDuplicateKeyUpdate()
                        .set(PROJECTS.NAME, it.name)
                        .set(PROJECTS.SHORT_NAME, it.shortName)
                        .set(PROJECTS.DESCRIPTION, it.description)
                        .execute()
                saveProjectCustomFields(it.shortName!!)
            }
        } catch (e: SocketTimeoutException) {
            println(e)
        } catch (e: DataAccessException) {
            println(e)
        }
        return result
    }

    override fun saveProjectCustomFields(id: String) {
        ProjectCustomFieldsRetrofitService.create().get(AUTH, id).execute().body()?.forEach { field ->
            val path = field.url.removePrefix(ROOT_REF)
            val req = ProjectCustomFieldParametersRetrofitService.create().get(AUTH, path)
            val res = req.execute().body()
            field.projectShortName = id
            field.fieldType = res?.type ?: ""
            field.emptyText = res?.emptyText ?: ""
            field.canBeEmpty = res?.canBeEmpty ?: false
            field.param = res?.param.toString()
            field.defaultValue = res?.defaultValue.toString()
            dslContext.insertInto(PROJECT_CUSTOM_FIELDS)
                    .set(PROJECT_CUSTOM_FIELDS.PROJECT_SHORT_NAME, field.projectShortName)
                    .set(PROJECT_CUSTOM_FIELDS.FIELD_NAME, field.name)
                    .set(PROJECT_CUSTOM_FIELDS.FIELD_URL, field.url)
                    .set(PROJECT_CUSTOM_FIELDS.FIELD_TYPE, field.fieldType)
                    .set(PROJECT_CUSTOM_FIELDS.EMPTY_TEXT, field.emptyText)
                    .set(PROJECT_CUSTOM_FIELDS.CAN_BE_EMPTY, field.canBeEmpty)
                    .set(PROJECT_CUSTOM_FIELDS.PARAM, field.param)
                    .set(PROJECT_CUSTOM_FIELDS.DEFAULT_VALUE, field.defaultValue)
                    .onDuplicateKeyUpdate()
                    .set(PROJECT_CUSTOM_FIELDS.FIELD_URL, field.url)
                    .set(PROJECT_CUSTOM_FIELDS.FIELD_TYPE, field.fieldType)
                    .set(PROJECT_CUSTOM_FIELDS.EMPTY_TEXT, field.emptyText)
                    .set(PROJECT_CUSTOM_FIELDS.CAN_BE_EMPTY, field.canBeEmpty)
                    .set(PROJECT_CUSTOM_FIELDS.PARAM, field.param)
                    .set(PROJECT_CUSTOM_FIELDS.DEFAULT_VALUE, field.defaultValue)
                    .executeAsync()
        }
    }
}