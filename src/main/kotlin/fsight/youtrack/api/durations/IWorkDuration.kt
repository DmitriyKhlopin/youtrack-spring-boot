package fsight.youtrack.api.durations

interface IWorkDuration {
    fun getByProject(projectShortName: String): List<Any>
}
