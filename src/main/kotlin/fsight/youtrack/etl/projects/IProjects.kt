package fsight.youtrack.etl.projects

import fsight.youtrack.models.YouTrackProject

interface IProjects {
    fun getProjects(): List<YouTrackProject>
    fun saveProjects(): Int
    fun saveProjectCustomFields(id: String)
}
