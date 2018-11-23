package fsight.youtrack.db.exposed.ms

data class ScheduleTimeIntervalModel(
    var id: Int? = null,
    var userSchedule: Int? = null,
    var week: Int? = null,
    var day: String? = null,
    var dateBegin: String? = null,
    var dateEnd: String? = null
)