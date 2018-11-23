package fsight.youtrack.db.exposed.ms.schedule.fact

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object WorkHoursTable : Table(name = "t_workhours") {
    val id: Column<Int> = integer(name = "id")
    val user: Column<Int> = integer(name = "user")
    val dateIn: Column<DateTime> = datetime(name = "date_in")
    val dateOut: Column<DateTime> = datetime(name = "date_out")
    val timeSourceId: Column<Int> = integer(name = "TimeSourceId")
    val cityId: Column<Int> = integer(name = "city_id")
}