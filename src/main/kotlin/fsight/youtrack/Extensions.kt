package fsight.youtrack

import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursModel
import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursTable
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalModel
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalTable
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedModel
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedView
import fsight.youtrack.etl.bundles.BundleValue
import fsight.youtrack.generated.jooq.tables.records.BundleValuesRecord
import fsight.youtrack.models.Field
import org.jetbrains.exposed.sql.ResultRow
import java.sql.Date
import java.sql.Timestamp
import java.util.*

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

fun List<Field>.getLong(fieldName: String): Long? {
    val filtered = this.filter { it.name == fieldName }
    return if (filtered.isNotEmpty()) filtered[0].value.toString().removeSurrounding("[", "]").toLong() else null
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
        cal.set(
            Calendar.DAY_OF_MONTH,
            cal.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_WEEK) + cal.firstDayOfWeek
        )
        val time = cal.timeInMillis
        time.toTimestamp()
    } else null
}