package fsight.youtrack.etl.timeline

import fsight.youtrack.generated.jooq.tables.IssueTimeline.ISSUE_TIMELINE
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.IssuesTimelineView.ISSUES_TIMELINE_VIEW
import fsight.youtrack.models.IssueTimelineItem
import fsight.youtrack.models.Schedule
import fsight.youtrack.toTimestamp
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class Timeline(private val dsl: DSLContext) : ITimeline {
    private val userStates = listOf(
        "На проверке", "Исправлена", "Решена", "Дубликат", "Не удается воспроизвести",
        "Проверена", "Подлежит проверке", "Требуется уточнение", "Готово", "Неполная", "Не существует",
        "Ожидает подтверждения", "Ожидает ответа", "Без подтверждения", "Подтверждена"
    )
    private val agentStates = listOf("Открыта", "backlog", "В обработке", "Зарегистрирована", "Создана")
    private val developerStates = listOf("Направлена разработчику", "Исследуется в JetBrains")

    override fun launchCalculation() {
        val i: List<String> = dsl
            .select(ISSUES.ID)
            .from(ISSUES)
            .where(ISSUES.LOADED_DATE.eq(Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay())).or(ISSUES.RESOLVED_DATE_TIME.isNull))
            .fetchInto(String::class.java)
        i.forEach { calculateForId(it) }
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
                    ISSUE_TIMELINE.TRANSITION_OWNER.eq("Agent").and(ISSUE_TIMELINE.ISSUE_ID.eq(ISSUES.ID))
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

    override fun calculateForId(issueId: String) {
        dsl.deleteFrom(ISSUE_TIMELINE).where(ISSUE_TIMELINE.ISSUE_ID.eq(issueId)).execute()
        val i: List<IssueTimelineItem> = dsl
            .select(
                ISSUES_TIMELINE_VIEW.ISSUE_ID.`as`("id"),
                ISSUES_TIMELINE_VIEW.UPDATE_DATE_TIME.`as`("date"),
                ISSUES_TIMELINE_VIEW.OLD_VALUE_STRING.`as`("stateOld"),
                ISSUES_TIMELINE_VIEW.NEW_VALUE_STRING.`as`("stateNew"),
                ISSUES_TIMELINE_VIEW.TIME_SPENT.`as`("timeSpent"),
                DSL.nullif(true, true).`as`("stateOwner")
            )
            .from(ISSUES_TIMELINE_VIEW)
            .where(ISSUES_TIMELINE_VIEW.ISSUE_ID.eq(issueId))
            .fetchInto(IssueTimelineItem::class.java)
        val schedule = Schedule("Стандартный", 1, 5, 11, 19)
        val a = arrayListOf<IssueTimelineItem>()
        i.forEachIndexed { index, issueTimelineItem ->
            val start = issueTimelineItem.date.toLocalDateTime()
            val end = (if (index + 1 >= i.size) issueTimelineItem.date else i[index + 1].date).toLocalDateTime()

            a.add(issueTimelineItem)
            if (start.toLocalDate() != end.toLocalDate()) {
                val calStart = Calendar.getInstance()
                calStart.set(
                    issueTimelineItem.date.toLocalDateTime().year,
                    issueTimelineItem.date.toLocalDateTime().monthValue - 1,
                    issueTimelineItem.date.toLocalDateTime().dayOfMonth,
                    0,
                    0,
                    0
                )
                calStart.set(Calendar.MILLISECOND, 0)
                calStart.add(Calendar.DATE, 1)
                calStart.add(Calendar.MILLISECOND, -1)
                a.add(
                    IssueTimelineItem(
                        issueId,
                        calStart.timeInMillis.toTimestamp(), /*"[Injected]", "[Injected]",*/
                        issueTimelineItem.stateNew,
                        issueTimelineItem.stateNew,
                        0L
                    )
                )

                val calEnd = Calendar.getInstance()
                calEnd.set(end.year, end.monthValue - 1, end.dayOfMonth, 0, 0, 0)
                calEnd.set(Calendar.MILLISECOND, 0)
                a.add(
                    IssueTimelineItem(
                        issueId,
                        calEnd.timeInMillis.toTimestamp(), /*"[Injected]", "[Injected]",*/
                        issueTimelineItem.stateNew,
                        issueTimelineItem.stateNew,
                        0L
                    )
                )
            }
        }

        a.forEachIndexed { index, issueTimelineItem ->
            issueTimelineItem.stateOwner = when {
                issueTimelineItem.stateOld in userStates -> "YouTrackUser"
                issueTimelineItem.stateOld in agentStates -> "Agent"
                issueTimelineItem.stateOld in developerStates -> "Developer"
                else -> "Undefined"
            }
            val start = (when (index) {
                0 -> issueTimelineItem.date
                in 1 until a.size -> a[index - 1].date
                else -> issueTimelineItem.date
            }).toLocalDateTime()
            val end = (when (index) {
                0 -> issueTimelineItem.date
                in 1 until a.size - 1 -> a[index].date
                else -> issueTimelineItem.date
            }).toLocalDateTime()
            val result: Long = when {
                /**Обе даты приходятся на один день.
                Начало и окончание вписываются в рабочие часы*/
                start.toLocalDate() == end.toLocalDate() && start.hour in schedule.firstHour..schedule.lastHour && end.hour in schedule.firstHour..schedule.lastHour -> {
                    /*println("Option 1")*/
                    Duration.between(start, end).toMillis() / 1000
                }
                /**Обе даты приходятся на один день.
                Начало до рабочих часов, окончание после*/
                start.toLocalDate() == end.toLocalDate() && start.hour < schedule.firstHour && end.hour > schedule.lastHour -> {
                    /*println("Option 2")*/
                    ((schedule.lastHour - schedule.firstHour + 1) * 3600).toLong()
                }
                /**Обе даты приходятся на один день.
                Начало и окончание после рабочих часов*/
                start.toLocalDate() == end.toLocalDate() && start.hour < schedule.firstHour && end.hour < schedule.firstHour -> {
                    /*println("Option 3")*/
                    0
                }
                /**Обе даты приходятся на один день.
                Начало и окончание после рабочих часов*/
                start.toLocalDate() == end.toLocalDate() && start.hour > schedule.lastHour && end.hour > schedule.lastHour -> {
                    /*println("Option 4")*/
                    0
                }
                /**Обе даты приходятся на один день.
                Начало в рабочее время, окончание после*/
                start.toLocalDate() == end.toLocalDate() && start.hour in schedule.firstHour..schedule.lastHour && end.hour > schedule.lastHour -> {
                    /*println("Option 5")*/
                    ((schedule.lastHour - start.toLocalTime().hour) * 3600 + (60 - start.toLocalTime().minute) * 60).toLong()
                }
                /**Обе даты приходятся на один день.
                Начало до рабочего времени, окончание в рабочее время*/
                start.toLocalDate() == end.toLocalDate() && start.hour < schedule.firstHour && end.hour in schedule.firstHour..schedule.lastHour -> {
                    /*println("Option 6")*/
                    ((end.hour - schedule.firstHour) * 3600 + end.minute * 60 + end.second).toLong()
                    /*((schedule.lastHour - start.toLocalTime().hour) * 3600 + (60 - start.toLocalTime().minute) * 60).toLong()*/
                }
                /**Разные даты*/
                start.toLocalDate() != end.toLocalDate() && Duration.between(start, end).toMillis() == 0L -> 0
                start.toLocalDate() != end.toLocalDate() -> Duration.between(
                    start,
                    end
                ).toDays() * (schedule.lastHour - schedule.firstHour + 1) * 3600
                else -> {
                    println("Unhandled case ${a[index].id} $start // $end")
                    0L
                }
            }
            a[index].timeSpent = result
        }
        a.forEachIndexed { index, it ->
            try {
                dsl.insertInto(ISSUE_TIMELINE)
                    .set(ISSUE_TIMELINE.ISSUE_ID, it.id)
                    .set(ISSUE_TIMELINE.STATE_FROM, it.stateOld)
                    .set(ISSUE_TIMELINE.STATE_TO, it.stateNew)
                    .set(ISSUE_TIMELINE.STATE_FROM_DATE, if (index == 0) it.date else a[index - 1].date)
                    .set(ISSUE_TIMELINE.STATE_TO_DATE, it.date)
                    .set(ISSUE_TIMELINE.TIME_SPENT, it.timeSpent)
                    .set(ISSUE_TIMELINE.TRANSITION_OWNER, it.stateOwner)
                    .execute()
            } catch (e: DataAccessException) {
                println(e.message)
            }
        }
    }
}
