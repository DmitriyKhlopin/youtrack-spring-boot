package fsight.youtrack.api.projects

import fsight.youtrack.models.ProjectModel

interface IProject {
    fun getProjects(): List<ProjectModel>
}