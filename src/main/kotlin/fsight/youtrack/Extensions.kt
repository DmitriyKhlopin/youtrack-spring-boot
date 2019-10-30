package fsight.youtrack

import com.google.gson.GsonBuilder
import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursModel
import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursTable
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalModel
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalTable
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedModel
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedView
import fsight.youtrack.generated.jooq.tables.records.BundleValuesRecord
import fsight.youtrack.models.BundleValue
import okhttp3.OkHttpClient
import org.jetbrains.exposed.sql.ResultRow
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.InetAddress
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

/*fun debugPrint(print: Boolean = false, message: String = "") {
    if (print) println(message)
}*/

fun ResultRow.toWorkHoursModel(): WorkHoursModel {
    return WorkHoursModel(
            id = this[WorkHoursTable.id],
            user = this[WorkHoursTable.user],
            dateIn = this[WorkHoursTable.dateIn].millis,
            dateOut = this[WorkHoursTable.dateOut].millis,
            timeSourceId = this[WorkHoursTable.timeSourceId],
            cityId = this[WorkHoursTable.cityId]
    )
}

fun ResultRow.toTimeAccountingExtendedModel() = TimeAccountingExtendedModel(
        this[TimeAccountingExtendedView.createDate].millis,
        this[TimeAccountingExtendedView.units],
        this[TimeAccountingExtendedView.agent],
        this[TimeAccountingExtendedView.changedDate].millis,
        this[TimeAccountingExtendedView.server],
        this[TimeAccountingExtendedView.projects],
        this[TimeAccountingExtendedView.teamProject],
        this[TimeAccountingExtendedView.id],
        this[TimeAccountingExtendedView.discipline],
        this[TimeAccountingExtendedView.person],
        this[TimeAccountingExtendedView.wit],
        this[TimeAccountingExtendedView.title],
        this[TimeAccountingExtendedView.iterationPath],
        this[TimeAccountingExtendedView.role],
        this[TimeAccountingExtendedView.projectType]
)

fun ResultRow.toScheduleTimeIntervalModel(): ScheduleTimeIntervalModel {
    return ScheduleTimeIntervalModel(
            id = this[ScheduleTimeIntervalTable.id],
            userSchedule = this[ScheduleTimeIntervalTable.userSchedule],
            week = this[ScheduleTimeIntervalTable.week],
            day = this[ScheduleTimeIntervalTable.day],
            dateBegin = this[ScheduleTimeIntervalTable.dateBegin],
            dateEnd = this[ScheduleTimeIntervalTable.dateEnd]
    )
}

fun Int?.toWorkTime(): String {
    if (this == null) return ""
    /*val minutes = this % 60*/
    val days = this / 480
    val hours = this / 60 - days * 8
    /*val minutesString = if (minutes > 0) "$minutes м. " else ""*/
    val hoursString = if (hours > 0) "$hours ч. " else ""
    val daysString = if (days > 0) "$days д. " else ""
    return "$daysString$hoursString"
}

fun BundleValue.toDatabaseRecord(): BundleValuesRecord =
        BundleValuesRecord()
                .setId(this.id)
                .setName(this.name)
                .setProjectId(this.projectId)
                .setProjectName(this.projectName)
                .setFieldId(this.fieldId)
                .setFieldName(this.fieldName)
                .setType(this.`$type`)

fun Boolean.invert() = !this

object IssueRequestMode {
    const val ALL = 0
    const val TODAY = 1
}

fun Long.toTimestamp() = Timestamp(this)

fun Long?.toDate(toStartOfTheWeek: Boolean = false): Timestamp? {
    return if (this != null) {
        val date = Date(this)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (toStartOfTheWeek) cal.set(
                Calendar.DAY_OF_MONTH,
                cal.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_WEEK) + cal.firstDayOfWeek
        )
        val time = cal.timeInMillis
        time.toTimestamp()
    } else null
}

fun String.toStartOfWeek(): Timestamp =
        Timestamp.valueOf(
                LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().minusDays(
                        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().dayOfWeek.value.toLong() - 1
                )
        )

fun String.toStartOfDate(): Timestamp =
        Timestamp.valueOf(LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())

fun String.toEndOfDate(): Timestamp =
        Timestamp.valueOf(LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))

fun String.splitToList(prefix: String = "[", suffix: String = "]", delimiters: String = ",") =
        this.removeSurrounding(prefix, suffix).split(delimiters)

fun debugPrint(message: String) {
    if (InetAddress.getLocalHost().hostName == "hlopind") {
        println(message)
    }
}

fun printlnIf(condition: Boolean, message: String?) {
    println(message)
}

fun printInLine(message: String) {
    print("${" ".repeat(200)}\r")
    print(message)
}

fun getOkhttpClient(timeout: Long = 30) = OkHttpClient().newBuilder()
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .build()

fun getConverterFactory(converter: Converter = Converter.SCALAR) = when (converter) {
    Converter.SCALAR -> {
        ScalarsConverterFactory.create()
    }
    else -> {
        val gson = GsonBuilder().setLenient().create()
        GsonConverterFactory.create(gson)
    }
}