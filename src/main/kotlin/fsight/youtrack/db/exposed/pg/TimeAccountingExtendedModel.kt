package fsight.youtrack.db.exposed.pg

data class TimeAccountingExtendedModel(
    var createDate: Long? = null,
    var units: Int? = null,
    var agent: String? = null,
    var changedDate: Long? = null,
    var server: Int? = null,
    var projects: String? = null,
    var teamProject: String? = null,
    var id: String? = null,
    var discipline: String? = null,
    var person: String? = null,
    var wit: String? = null,
    var title: String? = null,
    var iterationPath: String? = null,
    var role: String? = null,
    var projectType: String? = null
)