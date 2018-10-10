package fsight.youtrack.api.projects

import fsight.youtrack.models.ProjectModel

interface ProjectService {
    fun getProjects(): List<ProjectModel>
}