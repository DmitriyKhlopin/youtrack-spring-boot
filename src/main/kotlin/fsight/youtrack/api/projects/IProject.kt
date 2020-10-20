package fsight.youtrack.api.projects

import fsight.youtrack.models.YouTrackProject

interface IProject {
    fun getProjects(): List<YouTrackProject>
    fun attachWorkflow(data: ProjectWorkflow): Boolean
    fun attachCustomField(data: String): Any
}
