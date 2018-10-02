package fsight.youtrack.projects

import fsight.youtrack.AUTH
import fsight.youtrack.models.ProjectModel
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youtrack.jooq.tables.Projects
import youtrack.jooq.tables.Projects.PROJECTS
import java.sql.SQLException

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
        ProjectRetrofitService.create().getProjectsList(AUTH).execute().body()?.forEach {
            try {
                result = dslContext
                        .insertInto(Projects.PROJECTS)
                        .set(PROJECTS.NAME, it.name)
                        .set(PROJECTS.SHORT_NAME, it.shortName)
                        .set(PROJECTS.DESCRIPTION, it.description)
                        .onDuplicateKeyUpdate()
                        .set(PROJECTS.NAME, it.name)
                        .set(PROJECTS.SHORT_NAME, it.shortName)
                        .set(PROJECTS.DESCRIPTION, it.description)
                        .execute()
            } catch (e: SQLException) {
                println(e)
            }
        }
        return result
    }
}