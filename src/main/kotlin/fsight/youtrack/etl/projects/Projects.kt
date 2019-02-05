package fsight.youtrack.etl.projects

import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.YouTrackProject
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.SocketTimeoutException

@Service
@Transactional
class Projects(private val dslContext: DSLContext) : IProjects {
    override fun getProjects(): List<YouTrackProject> {
        return dslContext.select(
            PROJECTS.NAME.`as`("name"),
            PROJECTS.SHORT_NAME.`as`("shortName"),
            PROJECTS.SHORT_NAME.`as`("description"),
            PROJECTS.ID.`as`("id")
        )
            .from(PROJECTS)
            .fetchInto(YouTrackProject::class.java)
    }

    override fun saveProjects(): Int {
        var result = 0
        try {
            dslContext.deleteFrom(PROJECTS).execute()
            YouTrackAPI.create(Converter.GSON).getProjects(AUTH).execute().body()?.forEach {
                println(it)
                result = dslContext
                    .insertInto(PROJECTS)
                    .set(PROJECTS.NAME, it.name)
                    .set(PROJECTS.SHORT_NAME, it.shortName)
                    .set(PROJECTS.DESCRIPTION, it.description)
                    .set(PROJECTS.ID, it.id)
                    .onDuplicateKeyIgnore()
                    /*.set(PROJECTS.NAME, it.name)
                    .set(PROJECTS.SHORT_NAME, it.shortName)
                    .set(PROJECTS.DESCRIPTION, it.description)
                    .set(PROJECTS.ID, it.id)*/
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
        /*YouTrackAPI.createOld(Converter.GSON).getProjectCustomFields(AUTH, id).execute().body()?.forEach { field ->
            val path = field.url.removePrefix(ROOT_REF)
            val req = YouTrackAPI.createOld(Converter.GSON).getProjectCustomFieldParameters(AUTH, path)
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
        }*/
    }
}
