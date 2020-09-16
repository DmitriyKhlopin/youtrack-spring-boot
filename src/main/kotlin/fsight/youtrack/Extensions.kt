package fsight.youtrack

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.LongSerializationPolicy
import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursModel
import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursTable
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalModel
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalTable
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedModel
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedView
import fsight.youtrack.db.models.DevOpsFeature
import fsight.youtrack.generated.jooq.tables.records.BundleValuesRecord
import fsight.youtrack.models.BundleValue
import fsight.youtrack.models.DevOpsWorkItem
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.db.models.DevOpsStateOrder
import fsight.youtrack.models.youtrack.Issue
import okhttp3.OkHttpClient
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.InetAddress
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.TimeUnit


/*fun debugPrint(print: Boolean = false, message: String = "") {
    if (print) println(message)
}*/

fun getDevOpsUrl(systemId: Int): String = "<a href=\"https://tfsprod.fsight.ru/Foresight/AP/_workitems/edit/${systemId}\">${systemId}</a>"
fun getYouTrackUrl(issueId: String?): String = "<a href=\"https://support.fsight.ru/issue/${issueId}\">${issueId}</a>"

fun List<Issue>.getBugsAndFeatures(): List<Int> = this.map { it.bugsAndFeatures() }.flatten().distinct()

fun List<DevOpsWorkItem>.mergeWithHookData(hook: Hook, states: List<DevOpsStateOrder>): List<DevOpsWorkItem> {
    return this.map {
        if (it.systemId == hook.resource?.workItemId) {
            it.state = hook.getFieldValue("System.State").toString()
            it.sprint = hook.getFieldValue("System.IterationPath").toString()
        }
        /*it.sprintDates = dictionaries.sprints[it.sprint]*/
        it.stateOrder = states.firstOrNull { k -> k.state == it.state }?.order ?: -1
        it
    }
}

fun List<DevOpsWorkItem>.getSprints(): List<String> = this.map { it.sprint.substringAfterLast("\\") }.filterNot { it.contains("FY") }

fun List<DevOpsWorkItem>.getLastSprint(): String? {
    var sprint: String? = null
    try {
        sprint = this.map { it.sprint.substringAfterLast("\\") }.filterNot { it.contains("FY") }.maxBy { if (it == "Backlog") Int.MAX_VALUE else it.substringAfterLast(" ").toInt() }
    } catch (e: Exception) {

    }
    return sprint
}


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

fun Timestamp?.toStartOfDate(): Timestamp =
    Timestamp.valueOf(LocalDate.from(this?.toLocalDateTime()).atStartOfDay())

fun Timestamp?.toEndOfDate(): Timestamp =

    Timestamp.valueOf(LocalDate.from(this?.toLocalDateTime()).atTime(23, 59))

fun String.splitToList(prefix: String = "[", suffix: String = "]", delimiters: String = ",") =
    this.removeSurrounding(prefix, suffix).split(delimiters)

fun debugPrint(message: String) {
    if (InetAddress.getLocalHost().hostName == "hlopind") {
        println(message)
    }
}

fun printlnIf(condition: Boolean, message: String?) {
    if (condition) println(message)
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
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setLenient()
        gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING)
        val gson = gsonBuilder.create()

/*        val gson = GsonBuilder().setLenient().setLongSerializationPolicy(LongSerializationPolicy.STRING).create()*/
        GsonConverterFactory.create(gson)
    }
}

class ExposedTransformations {
    val toList: (ResultSet) -> List<String> = { rs ->
        val list = arrayListOf<String>()
        while (rs.next()) {
            val s = (1..rs.metaData.columnCount).joinToString { index -> rs.getString(index) ?: "null" }
            list.add(s)
        }
        list

    }
    val getSingleProperty: (ResultSet, String) -> String = { rs, prop -> rs.getString(prop) }
    val getPair: (ResultSet, String, String) -> Pair<Int, String> = { rs, prop1, prop2 -> Pair(first = rs.getString(prop1).toInt(), second = rs.getString(prop2)) }
    val toJsonObject: (ResultSet, List<String>) -> JsonObject = { rs, props ->
        val j = JsonObject()
        props.forEach { i ->
            run {
                j.addProperty(i, rs.getString(i))
            }
        }
        j
    }

    val toDevOpsWorkItem: (ResultSet) -> DevOpsWorkItem = { rs ->
        DevOpsWorkItem(
            systemId = rs.getString("System_Id").toInt(),
            state = rs.getString("System_State"),
            sprint = rs.getString("IterationPath"),
            sprintDates = null,
            priority = rs.getString("Microsoft_VSTS_Common_Priority"),
            createdDate = rs.getTimestamp("System_CreatedDate"),
            assignee = rs.getString("System_AssignedTo"),
            type = rs.getString("System_WorkItemType"),
            area = rs.getString("AreaPath"),
            title = rs.getString("System_Title"),
            author = rs.getString("System_CreatedBy") ?: null
        )
    }

    val toDevOpsFeature: (ResultSet) -> DevOpsFeature = { rs ->
        DevOpsFeature(
            id = rs.getString("System_Id").toInt(),
            priority = rs.getString("Microsoft_VSTS_Common_Priority").toInt(),
            createdDate = rs.getTimestamp("System_CreatedDate"),
            updatedDate = rs.getTimestamp("System_ChangedDate"),
            assignee = rs.getString("System_AssignedTo"),
            title = rs.getString("System_Title")
        )
    }
}


fun <T : Any> String.execAndMap(db: Database?, transform: (ResultSet) -> T): List<T> {
    val result = arrayListOf<T>()
    val statement = this
    transaction(db) {
        TransactionManager.current().execCTE(statement) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
    }
    return result
}

fun <T : Any> Transaction.execCTE(stmt: String, transform: (ResultSet) -> T): T? {
    if (stmt.isEmpty()) return null

    val type = StatementType.SELECT

    return exec(object : Statement<T>(type, emptyList()) {
        override fun PreparedStatement.executeInternal(transaction: Transaction): T? {
            executeQuery()
            return resultSet?.let {
                try {
                    transform(it)
                } finally {
                    it.close()
                }
            }
        }

        override fun prepareSQL(transaction: Transaction): String = stmt

        override fun arguments(): Iterable<Iterable<Pair<ColumnType, Any?>>> = emptyList()
    })
}

fun LocalDate.toTimestamp(): Timestamp = Timestamp.valueOf(this.atStartOfDay())

fun Timestamp.toDateRanges(rangesCount: Int, rangeLength: Int): List<Pair<Timestamp, Timestamp>> {
    if (rangeLength < 0) return listOf()
    val month = Date(this.time).toLocalDate().monthValue
    val i = Date(this.time).toLocalDate().plusMonths((rangeLength - if (month % rangeLength == 0) rangeLength else month % rangeLength).toLong()).with(ChronoField.DAY_OF_MONTH, 1)
    return (0 until rangesCount).mapIndexed { index, _ ->
        Pair(
            i.minusMonths(rangeLength.toLong() * (index + 1) - 1).toTimestamp(),
            i.minusMonths(rangeLength.toLong() * index - 1).minusDays(1).toTimestamp()
        )
    }
}
