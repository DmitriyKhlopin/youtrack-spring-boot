package fsight.youtrack.api.projects

import fsight.youtrack.generated.jooq.tables.Projects.PROJECTS
import fsight.youtrack.models.ProjectModel
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class ProjectImplementation(private val dslContext: DSLContext) : ProjectService {
    override fun getProjects(): List<ProjectModel> {
        return dslContext.select(
                PROJECTS.NAME.`as`("name"),
                PROJECTS.SHORT_NAME.`as`("shortName")
        ).from(PROJECTS).fetchInto(ProjectModel::class.java)
    }
}