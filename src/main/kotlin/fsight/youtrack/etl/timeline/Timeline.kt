package fsight.youtrack.etl.timeline

import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.IssueTimeline.ISSUE_TIMELINE
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.models.IssueTimelineItem
import fsight.youtrack.models.Schedule
import fsight.youtrack.models.toIssueTimelineRecord
import fsight.youtrack.toStartOfDate
import fsight.youtrack.toTimestamp
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class Timeline(private val dsl: DSLContext) : ITimeline {
    @Autowired
    private lateinit var pg: IPGProvider;

    private val schedule = Schedule("Стандартный", 1, 5, 11, 19)
    private val userStates = listOf(
        "На проверке", "Исправлена", "Решена", "Дубликат", "Не удается воспроизвести",
        "Проверена", "Подлежит проверке", "Требуется уточнение", "Готово", "Неполная", "Не существует",
        "Ожидает подтверждения", "Ожидает ответа", "Без подтверждения", "Подтверждена"
    )
    private val agentStates = listOf("Открыта", "backlog", "В обработке", "Зарегистрирована", "Создана")
    private val developerStates = listOf("Направлена разработчику", "Исследуется в JetBrains")

    private val holidays = listOf(
        "2018-11-05", "2019-01-01", "2019-01-02", "2019-01-03",
        "2019-01-04", "2019-01-07", "2019-01-08", "2019-03-08",
        "2019-05-01", "2019-05-02", "2019-05-03", "2019-05-09",
        "2019-05-10", "2019-06-12", "2019-11-04", "2020-01-01",
        "2020-01-02", "2020-01-03", "2020-01-06", "2020-01-07",
        "2020-01-08", "2020-02-24", "2020-03-09", "2020-05-01",
        "2020-05-04", "2020-05-05", "2020-05-11", "2020-06-12",
        "2020-11-04", "2021-01-01", "2021-01-02", "2021-01-04",
        "2021-01-05", "2021-01-06", "2021-01-07", "2021-01-08",
        "2021-02-23", "2021-03-08", "2021-05-03", "2021-05-10",
        "2021-06-14", "2021-11-04"
    ).map { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }

    override fun launchCalculation() {
        val i: List<String> = dsl
            .select(ISSUES.ID)
            .from(ISSUES)
            .where(ISSUES.UPDATED_DATE_TIME.ge(Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay())).or(ISSUES.RESOLVED_DATE_TIME.isNull))
            .and(ISSUES.PROJECT_SHORT_NAME.notIn(listOf("SD", "TC", "SPAM", "PO", "TEST", "SPAM")))
            .fetchInto(String::class.java)
        println("Need to calculate timelines for ${i.size} items.")
        val size = i.size
        i.asSequence().forEachIndexed { index, s ->
            calculateForId(s, index, size, false)
            println("Calculated timeline for ${index * 100 / size}% of issues")
        }
        println("Calculated timeline for 100% of issues")
        pg.updateAllIssuesSpentTime()
    }

    override fun launchCalculationForPeriod(dateFrom: String?, dateTo: String?) {
        val df = dateFrom?.toStartOfDate() ?: return
        val dt = dateTo?.toStartOfDate() ?: return
        val i: List<String> = pg.getIssuesUpdatedInPeriod(df, dt)
        val size = i.size
        println("Need to calculate timelines for $size items.")
        i.asSequence().forEachIndexed { index, s ->
            calculateForId(s, index, size, true)
            print("Calculated timeline for ${index * 100 / size}% of issues\r")
        }
        pg.updateAllIssuesSpentTime()
    }

    override fun calculateForId(issueId: String, currentIndex: Int, issuesSize: Int, update: Boolean): List<IssueTimelineItem> {
        println("Calculating timeline for $issueId")
        dsl.deleteFrom(ISSUE_TIMELINE).where(ISSUE_TIMELINE.ISSUE_ID.eq(issueId)).execute()
        val i: List<IssueTimelineItem> = pg.getIssueTimelineItemsById(issueId)
        val a = arrayListOf<IssueTimelineItem>()
        i.forEachIndexed { index, issueTimelineItem ->
            issueTimelineItem.dateFrom = if (index - 1 >= 0) i[index - 1].dateTo else issueTimelineItem.dateTo
            val start = issueTimelineItem.dateFrom.toLocalDateTime()
            val end = issueTimelineItem.dateTo.toLocalDateTime()

            if (start.toLocalDate() == end.toLocalDate()) {
                a.add(issueTimelineItem)
            } else {
                /**Интервал от начала перехода до конца дня*/
                val pref = issueTimelineItem.copy()
                val prefDateTo = Calendar.getInstance()
                prefDateTo.set(
                    pref.dateFrom.toLocalDateTime().year,
                    pref.dateFrom.toLocalDateTime().monthValue - 1,
                    pref.dateFrom.toLocalDateTime().dayOfMonth,
                    0,
                    0,
                    0
                )
                prefDateTo.add(Calendar.DATE, 1)
                prefDateTo.set(Calendar.MILLISECOND, 0)
                prefDateTo.add(Calendar.MILLISECOND, -1)

                pref.dateTo = prefDateTo.timeInMillis.toTimestamp()
                pref.stateNew = pref.stateOld
                a.add(pref)


                val dateFrom = Calendar.getInstance()
                dateFrom.set(
                    issueTimelineItem.dateFrom.toLocalDateTime().year,
                    issueTimelineItem.dateFrom.toLocalDateTime().monthValue - 1,
                    issueTimelineItem.dateFrom.toLocalDateTime().dayOfMonth, 0, 0, 0
                )

                val dateTo = Calendar.getInstance()
                dateTo.set(
                    issueTimelineItem.dateTo.toLocalDateTime().year,
                    issueTimelineItem.dateTo.toLocalDateTime().monthValue - 1,
                    issueTimelineItem.dateTo.toLocalDateTime().dayOfMonth, 0, 0, 0
                )

                val j = Duration.between(
                    dateFrom.timeInMillis.toTimestamp().toLocalDateTime(),
                    dateTo.timeInMillis.toTimestamp().toLocalDateTime()
                ).toDays() - 1
                for (n in 1..j) {
                    val k = issueTimelineItem.copy()
                    val startOfDay = Calendar.getInstance()
                    startOfDay.set(
                        issueTimelineItem.dateFrom.toLocalDateTime().year,
                        issueTimelineItem.dateFrom.toLocalDateTime().monthValue - 1,
                        issueTimelineItem.dateFrom.toLocalDateTime().dayOfMonth, 0, 0, 0
                    )
                    startOfDay.set(Calendar.MILLISECOND, 0)
                    startOfDay.add(Calendar.DATE, n.toInt())

                    val endOfDay = Calendar.getInstance()
                    endOfDay.set(
                        issueTimelineItem.dateFrom.toLocalDateTime().year,
                        issueTimelineItem.dateFrom.toLocalDateTime().monthValue - 1,
                        issueTimelineItem.dateFrom.toLocalDateTime().dayOfMonth, 0, 0, 0
                    )
                    endOfDay.set(Calendar.MILLISECOND, 0)
                    endOfDay.add(Calendar.MILLISECOND, -1)
                    endOfDay.add(Calendar.DATE, n.toInt() + 1)

                    k.dateFrom = startOfDay.timeInMillis.toTimestamp()
                    k.dateTo = endOfDay.timeInMillis.toTimestamp()
                    k.stateNew = k.stateOld
                    a.add(k)
                }

                /**Интервал от начала дня до конца перехода*/
                val post = issueTimelineItem.copy()
                val postDateFrom = Calendar.getInstance()
                postDateFrom.set(
                    post.dateTo.toLocalDateTime().year,
                    post.dateTo.toLocalDateTime().monthValue - 1,
                    post.dateTo.toLocalDateTime().dayOfMonth,
                    0,
                    0,
                    0
                )
                postDateFrom.set(Calendar.MILLISECOND, 0)
                post.dateFrom = postDateFrom.timeInMillis.toTimestamp()
                a.add(post)
            }
        }

        a.forEachIndexed { index, it ->
            it.dateFrom = if (index == 0) it.dateTo else a[index - 1].dateTo
            it.stateOwner = when (it.stateOld) {
                in userStates -> "YouTrackUser"
                in agentStates -> "Agent"
                in developerStates -> "Developer"
                else -> "Undefined"
            }
            val start = it.dateFrom.toLocalDateTime()
            val end = (it.dateTo).toLocalDateTime()
            val result: Long = when {
                start.toLocalDate() in holidays -> 0
                start.dayOfWeek.value !in schedule.firstDay..schedule.lastDay -> 0
                /**Обе даты приходятся на один день.
                Начало и окончание вписываются в рабочие часы*/
                start.toLocalDate() == end.toLocalDate() && start.hour in schedule.firstHour..schedule.lastHour && end.hour in schedule.firstHour..schedule.lastHour ->
                    Duration.between(start, end).toMillis() / 1000
                /**Обе даты приходятся на один день.
                Начало до рабочих часов, окончание после*/
                start.toLocalDate() == end.toLocalDate() && start.hour < schedule.firstHour && end.hour > schedule.lastHour -> ((schedule.lastHour - schedule.firstHour + 1) * 3600).toLong()
                /**Обе даты приходятся на один день.
                Начало и окончание после рабочих часов*/
                start.toLocalDate() == end.toLocalDate() && start.hour < schedule.firstHour && end.hour < schedule.firstHour -> 0
                /**Обе даты приходятся на один день.
                Начало и окончание после рабочих часов*/
                start.toLocalDate() == end.toLocalDate() && start.hour > schedule.lastHour && end.hour > schedule.lastHour -> 0
                /**Обе даты приходятся на один день.
                Начало в рабочее время, окончание после*/
                start.toLocalDate() == end.toLocalDate() && start.hour in schedule.firstHour..schedule.lastHour && end.hour > schedule.lastHour ->
                    ((schedule.lastHour - start.toLocalTime().hour) * 3600 + (60 - start.toLocalTime().minute) * 60).toLong()
                /**Обе даты приходятся на один день.
                Начало до рабочего времени, окончание в рабочее время*/
                start.toLocalDate() == end.toLocalDate() && start.hour < schedule.firstHour && end.hour in schedule.firstHour..schedule.lastHour ->
                    ((end.hour - schedule.firstHour) * 3600 + end.minute * 60 + end.second).toLong()
                /**Разные даты*/
                start.toLocalDate() != end.toLocalDate() && Duration.between(start, end).toMillis() == 0L -> 0
                start.toLocalDate() != end.toLocalDate() ->
                    Duration.between(start, end).toDays() * (schedule.lastHour - schedule.firstHour + 1) * 3600
                else -> {
                    println("Unhandled case ${a[index].id} $start // $end")
                    println()
                    0L
                }
            }
            a[index].timeSpent = result
        }
        val stored = pg.saveIssueTimelineItems(a)
        println("Saved $stored timeline items")
        return a
    }
}
