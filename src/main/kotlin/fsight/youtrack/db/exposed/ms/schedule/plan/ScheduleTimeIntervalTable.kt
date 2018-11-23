package fsight.youtrack.db.exposed.ms.schedule.plan

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object ScheduleTimeIntervalTable : Table(name = "t_shedule_timeinterval") {
    val id: Column<Int> = integer(name = "id")
    val userSchedule: Column<Int> = integer(name = "user_shedule")
    val week: Column<Int> = integer(name = "week")
    val day: Column<String> = varchar(name = "day", length = 15)
    val dateBegin: Column<String> = varchar(name = "date_begin", length = 5)
    val dateEnd: Column<String> = varchar(name = "date_end", length = 5)
}

