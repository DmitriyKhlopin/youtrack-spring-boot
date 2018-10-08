package fsight.youtrack.etl.projects

import fsight.youtrack.AUTH
import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.ProjectModel
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.SocketTimeoutException

@Service
@Transactional
class ProjectsService(private val dslContext: DSLContext) : ProjectsInterface {

    override fun getProjects(): List<ProjectModel> {
        return dslContext.select(
                PROJECTS.NAME.`as`("name"),
                PROJECTS.SHORT_NAME.`as`("shortName"),
                PROJECTS.SHORT_NAME.`as`("description"))
                .from(PROJECTS)
                .fetchInto(ProjectModel::class.java)
    }

    override fun updateProjects(): Int {
        var result = 0
        try {
            ProjectRetrofitService.create().getProjectsList(AUTH).execute().body()?.forEach {
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
            }
        } catch (e: SocketTimeoutException) {
            println(e)
        } catch (e: DataAccessException) {
            println(e)
        }
        return result
    }
}