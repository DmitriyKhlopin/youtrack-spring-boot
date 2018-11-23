package fsight.youtrack.db.exposed.ms.schedule.fact

data class WorkHoursModel(
    val id: Int? = null,
    val user: Int? = null,
    val dateIn: Long? = null,
    val dateOut: Long? = null,
    val timeSourceId: Int? = null,
    val cityId: Int? = null
)