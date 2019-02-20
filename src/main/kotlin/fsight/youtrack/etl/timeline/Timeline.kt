package fsight.youtrack.etl.timeline

import fsight.youtrack.generated.jooq.tables.IssueTimeline.ISSUE_TIMELINE
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.IssuesTimelineView.ISSUES_TIMELINE_VIEW
import fsight.youtrack.models.IssueTimelineItem
import fsight.youtrack.models.Schedule
import fsight.youtrack.models.toIssueTimelineRecord
import fsight.youtrack.toTimestamp
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class Timeline(private val dsl: DSLContext) : ITimeline {
    private val schedule = Schedule("Стандартный", 1, 5, 11, 19)
    private val userStates = listOf(
        "На проверке", "Исправлена", "Решена", "Дубликат", "Не удается воспроизвести",
        "Проверена", "Подлежит проверке", "Требуется уточнение", "Готово", "Неполная", "Не существует",
        "Ожидает подтверждения", "Ожидает ответа", "Без подтверждения", "Подтверждена"
    )
    private val agentStates = listOf("Открыта", "backlog", "В обработке", "Зарегистрирована", "Создана")
    private val developerStates = listOf("Направлена разработчику", "Исследуется в JetBrains")

    private val holidays = listOf<LocalDate>(
        LocalDate.parse("2018-11-05", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-01-01", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-01-02", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-01-03", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-01-04", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-01-07", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-01-08", DateTimeFormatter.ISO_DATE),
        LocalDate.parse("2019-03-08", DateTimeFormatter.ISO_DATE)
    )

    override fun launchCalculation() {
        val i: List<String> = dsl
            .select(ISSUES.ID)
            .from(ISSUES)
            .where(ISSUES.LOADED_DATE.ge(Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay())).or(ISSUES.RESOLVED_DATE_TIME.isNull))
            .fetchInto(String::class.java)
        println("Need to calculate timelines for ${i.size} items.")
        i.asSequence().forEach { calculateForId(it) }
        /*updateIssueSpentTime()*/
    }

    fun updateIssueSpentTime() {
        dsl.update(ISSUES)
            .set(
                ISSUES.TIME_USER,
                DSL.select(DSL.sum(ISSUE_TIMELINE.TIME_SPENT)).from(ISSUE_TIMELINE).where(
                    ISSUE_TIMELINE.TRANSITION_OWNER.eq("YouTrackUser").and(ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID))
                ).asField<Long>()
            )
            .set(
                ISSUES.TIME_AGENT,
                DSL.select(DSL.sum(ISSUE_TIMELINE.TIME_SPENT)).from(ISSUE_TIMELINE).where(
                    ISSUE_TIMELINE.TRANSITION_OWNER.`in`(listOf("Agent", "Undefined")).and(
                        ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID)
                    )
                ).asField<Long>()
            )
            .set(
                ISSUES.TIME_DEVELOPER,
                DSL.select(DSL.sum(ISSUE_TIMELINE.TIME_SPENT)).from(ISSUE_TIMELINE).where(
                    ISSUE_TIMELINE.TRANSITION_OWNER.eq("Developer").and(ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID))
                ).asField<Long>()
            )
            .execute()
    }

    fun updateIssueSpentTimeById(issueId: String) {
        dsl.update(ISSUES)
            .set(
                ISSUES.TIME_USER,
                DSL.select(DSL.sum(ISSUE_TIMELINE.TIME_SPENT)).from(ISSUE_TIMELINE).where(
                    ISSUE_TIMELINE.TRANSITION_OWNER.eq("YouTrackUser").and(ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID))
                ).asField<Long>()
            )
            .set(
                ISSUES.TIME_AGENT,
                DSL.select(DSL.sum(ISSUE_TIMELINE.TIME_SPENT)).from(ISSUE_TIMELINE).where(
                    ISSUE_TIMELINE.TRANSITION_OWNER.`in`(listOf("Agent", "Undefined")).and(
                        ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID)
                    )
                ).asField<Long>()
            )
            .set(
                ISSUES.TIME_DEVELOPER,
                DSL.select(DSL.sum(ISSUE_TIMELINE.TIME_SPENT)).from(ISSUE_TIMELINE).where(
                    ISSUE_TIMELINE.TRANSITION_OWNER.eq("Developer").and(ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID))
                ).asField<Long>()
            )
            .where(ISSUES.ID.eq(issueId))
            .execute()
    }

    override fun calculateForId(issueId: String): List<IssueTimelineItem> {
        println("calculating timeline for $issueId")
        dsl.deleteFrom(ISSUE_TIMELINE).where(ISSUE_TIMELINE.ISSUE_ID.eq(issueId)).execute()
        val i: List<IssueTimelineItem> = dsl
            .select(
                ISSUES_TIMELINE_VIEW.ISSUE_ID.`as`("id"),
                ISSUES_TIMELINE_VIEW.UPDATE_DATE_TIME.`as`("dateFrom"),
                ISSUES_TIMELINE_VIEW.UPDATE_DATE_TIME.`as`("dateTo"),
                ISSUES_TIMELINE_VIEW.OLD_VALUE_STRING.`as`("stateOld"),
                ISSUES_TIMELINE_VIEW.NEW_VALUE_STRING.`as`("stateNew"),
                ISSUES_TIMELINE_VIEW.TIME_SPENT.`as`("timeSpent"),
                DSL.nullif(true, true).`as`("stateOwner")
            )
            .from(ISSUES_TIMELINE_VIEW)
            .where(ISSUES_TIMELINE_VIEW.ISSUE_ID.eq(issueId))
            .fetchInto(IssueTimelineItem::class.java)

        val a = arrayListOf<IssueTimelineItem>()
        i.forEachIndexed { index, issueTimelineItem ->
            /*println(index)*/
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
            /*println(it)*/
            it.dateFrom = if (index == 0) it.dateTo else a[index - 1].dateTo
            it.stateOwner = when {
                it.stateOld in userStates -> "YouTrackUser"
                it.stateOld in agentStates -> "Agent"
                it.stateOld in developerStates -> "Developer"
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
                    0L
                }
            }
            a[index].timeSpent = result
        }
        val stored = dsl.loadInto(ISSUE_TIMELINE).loadRecords(a.map(IssueTimelineItem::toIssueTimelineRecord)).fields(
            ISSUE_TIMELINE.ISSUE_ID,
            ISSUE_TIMELINE.STATE_FROM,
            ISSUE_TIMELINE.STATE_TO,
            ISSUE_TIMELINE.STATE_FROM_DATE,
            ISSUE_TIMELINE.STATE_TO_DATE,
            ISSUE_TIMELINE.TIME_SPENT,
            ISSUE_TIMELINE.TRANSITION_OWNER
        ).execute().stored()
        println("$issueId: stored $stored timeline items")
        updateIssueSpentTimeById(issueId)
        return a
    }
}
