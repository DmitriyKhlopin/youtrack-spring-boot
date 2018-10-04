package fsight.youtrack

import fsight.youtrack.models.Field
import java.sql.Date
import java.sql.Timestamp
import java.util.*

fun Boolean.invert() = !this

object IssueRequestMode {
    const val ALL = 0
    const val TODAY = 1
}

fun List<Field>.getLong(fieldName: String): Long? {
    val filtered = this.filter { it.name == fieldName }
    return if (filtered.isNotEmpty()) filtered[0].value
            .toString().removeSurrounding("[", "]")
            .toLong() else null
}

fun List<Field>.getString(fieldName: String): String? {
    val filtered = this.filter { it.name == fieldName }
    return if (filtered.isNotEmpty()) filtered[0].value.toString().removeSurrounding("[", "]") else null
}

fun List<Field>.getInt(fieldName: String): Int? {
    val filtered = this.filter { it.name == fieldName }
    return filtered[0].value.toString().toIntOrNull()
}

fun Long.toTimestamp() = Timestamp(this)

fun Long?.toDate(): Timestamp? {
    return if (this != null) {
        val date = Date(this)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val time = cal.timeInMillis
        time.toTimestamp()
    } else null
}

fun Long?.toWeek(): Timestamp? {
    return if (this != null) {
        val date = Date(this)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_WEEK) + cal.firstDayOfWeek)
        val time = cal.timeInMillis
        time.toTimestamp()
    } else null
}

/*
val DbModule = applicationContext {
    bean { DbServiceImpl() }
}*/
