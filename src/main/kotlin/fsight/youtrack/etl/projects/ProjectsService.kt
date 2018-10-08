package fsight.youtrack.etl.projects

import fsight.youtrack.models.ProjectModel

interface ProjectsService {
    fun getProjects(): List<ProjectModel>
    fun saveProjects(): Int
    fun saveProjectCustomFields(id:String)
}