package fsight.youtrack.models

import java.sql.Timestamp

class TimeAccountingItem(
    var crDate: Timestamp?,
    var units: Int?,
    var agent: String?,
    var changedDate: Timestamp?,
    var server: Int?,
    var projects: String?,
    var teamProject: String?,
    var id: String?,
    var discipline: String?,
    var person: String?,
    var wit: String?,
    var title: String?,
    var iterationPath: String?,
    var role: String?
)
