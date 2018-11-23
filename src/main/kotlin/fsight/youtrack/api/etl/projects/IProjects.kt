package fsight.youtrack.api.etl.projects

import fsight.youtrack.models.ProjectModel

interface IProjects {
    fun getProjects(): List<ProjectModel>
    fun saveProjects(): Int
    fun saveProjectCustomFields(id: String)
}