package fsight.youtrack.models

data class Schedule(
    val name: String,
    val firstDay: Int,
    val lastDay: Int,
    val firstHour: Int,
    val lastHour: Int
)
