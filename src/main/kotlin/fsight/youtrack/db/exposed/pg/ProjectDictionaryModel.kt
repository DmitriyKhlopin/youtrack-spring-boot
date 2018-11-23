package fsight.youtrack.db.exposed.pg

data class ProjectDictionaryModel(
    var projectShortName: String? = null,
    var customer: String? = null,
    var projectEts: String? = null,
    var iterationPath: String? = null,
    var dateFrom: Long,
    var dateTo: Long
)
