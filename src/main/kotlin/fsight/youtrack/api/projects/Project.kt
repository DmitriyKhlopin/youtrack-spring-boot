package fsight.youtrack.api.projects

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
}
