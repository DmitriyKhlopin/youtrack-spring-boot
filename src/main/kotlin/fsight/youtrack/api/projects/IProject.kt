package fsight.youtrack.api.projects

import fsight.youtrack.models.YouTrackProject

interface IProject {
    fun getProjects(): List<YouTrackProject>
}
