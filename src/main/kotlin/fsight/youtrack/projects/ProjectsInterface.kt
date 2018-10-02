package fsight.youtrack.projects

import fsight.youtrack.models.ProjectModel

interface ProjectsInterface {
    fun getProjects(): List<ProjectModel>
    fun updateProjects(): Int
}