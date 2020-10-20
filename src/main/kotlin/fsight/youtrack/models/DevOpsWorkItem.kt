package fsight.youtrack.models

import java.sql.Timestamp

data class DevOpsWorkItem(
    var systemId: Int,
    var state: String,
    var sprint: String,
    var sprintDates: Pair<Timestamp, Timestamp>?,
    var stateOrder: Int = -1,
    var priority: String? = null,
    var createdDate: Timestamp? = null,
    var updatedDate: Timestamp? = null,
    var assignee: String? = null,
    var type: String? = null,
    var area: String? = null,
    var title: String? = null,
    var author: String? = null,
    var foundIn: String? = null,
    var integratedIn: String? = null,
    var reason: String? = null,
    var severity: String? = null,
    var team: String? = null,
    var teamPo: String? = null
)
