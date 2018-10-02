package fsight.youtrack.models

data class ProjectModel(
        var name: String? = null,
        var shortName: String? = null,
        var description: String? = null,
        var versions: String? = null,
        var subsystems: String? = null,
        var assigneesFullName: String? = null
)