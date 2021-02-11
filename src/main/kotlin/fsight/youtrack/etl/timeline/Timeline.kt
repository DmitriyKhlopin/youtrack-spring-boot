package fsight.youtrack.etl.timeline

import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.models.IssueTimelineItem
import fsight.youtrack.models.Schedule
import fsight.youtrack.toEndOfDate
import fsight.youtrack.toStartOfDate
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    private val extraWorkingDays = listOf(
        "2018-06-09"
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
            calculateDetailedStateByIssueId(s)
            print("Calculated timeline for ${index * 100 / size}% of issues\r")
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
            calculateStateByIssueId(s)
            print("Calculated timeline for ${index * 100 / size}% of issues\r")
        }
        pg.updateAllIssuesSpentTime()
    }

    override fun calculateStateByIssueId(issueId: String) {
        val i = pg.getIssueTimelineById(issueId)
        i.forEach {
            it.timeSpent = calculatePeriod(it)
        }
        pg.saveIssueTimelineItems(i)
    }


    override fun launchDetailedCalculationForPeriod(dateFrom: String?, dateTo: String?) {
        val df = dateFrom?.toStartOfDate() ?: return
        val dt = dateTo?.toStartOfDate() ?: return
        val i: List<String> = pg.getIssuesUpdatedInPeriod(df, dt)
        val size = i.size
        println("Need to calculate detailed timelines for $size items.")
        i.asSequence().forEachIndexed { index, s ->
            calculateDetailedStateByIssueId(s)
            print("Calculated detailed timeline for ${index * 100 / size}% of issues\r")
        }
    }

    override fun calculateDetailedStateByIssueId(issueId: String) {
        val i = pg.getIssuesDetailedTimelineById(issueId)
        i.forEach {
            it.timeSpent = calculatePeriod(it)
        }
        pg.saveIssueTimelineDetailedItems(i)
    }

    override fun calculatePeriod(item: IssueTimelineItem): Int {
        val start = item.dateFrom.toLocalDateTime()
        val end = item.dateTo.toLocalDateTime()
        val a = arrayListOf<IssueTimelineItem>()
        /*Время между датами начали и конца периода*/
        var agg = 0;
        if (start.toLocalDate() == end.toLocalDate()) {
            a.add(item)
        } else {
            /**Интервал от начала перехода до конца дня*/
            val startingInterval = item.copy()
            startingInterval.dateTo = startingInterval.dateFrom.toEndOfDate()
            a.add(startingInterval)

            /**Интервал от начала дня до конца перехода*/
            val endingInterval = item.copy()
            endingInterval.dateFrom = endingInterval.dateTo.toStartOfDate()
            a.add(endingInterval)

            /**Дни между началом и концом периода*/
            val dateFrom = item.dateFrom.toStartOfDate().toLocalDateTime().plusDays(1)
            val dateTo = item.dateTo.toEndOfDate().toLocalDateTime().minusDays(1)
            val j = Duration.between(dateFrom, dateTo).toDays()
            for (n in 0 until j) {
                val df = dateFrom.plusDays(n).toLocalDate()
                when {
                    df in holidays -> {
                    }
                    df in extraWorkingDays -> agg += (schedule.lastHour - schedule.firstHour + 1) * 3600
                    df.dayOfWeek.value !in schedule.firstDay..schedule.lastDay -> {
                    }
                    else -> agg += (schedule.lastHour - schedule.firstHour + 1) * 3600
                }
            }
        }
        a.forEachIndexed { index, it ->
            val s = it.dateFrom.toLocalDateTime()
            val e = (it.dateTo).toLocalDateTime()
            val sd = s.toLocalDate()
            val ed = e.toLocalDate()
            val result: Long = when {
                sd in holidays -> 0
                s.dayOfWeek.value !in schedule.firstDay..schedule.lastDay -> 0
                /**Обе даты приходятся на один день.
                Начало и окончание вписываются в рабочие часы*/
                sd == ed && s.hour in schedule.firstHour..schedule.lastHour && e.hour in schedule.firstHour..schedule.lastHour ->
                    Duration.between(s, e).toMillis() / 1000
                /**Обе даты приходятся на один день.
                Начало до рабочих часов, окончание после*/
                sd == ed && s.hour < schedule.firstHour && e.hour > schedule.lastHour -> ((schedule.lastHour - schedule.firstHour + 1) * 3600).toLong()
                /**Обе даты приходятся на один день.
                Начало и окончание после рабочих часов*/
                sd == ed && s.hour < schedule.firstHour && e.hour < schedule.firstHour -> 0
                /**Обе даты приходятся на один день.
                Начало и окончание после рабочих часов*/
                sd == ed && s.hour > schedule.lastHour && e.hour > schedule.lastHour -> 0
                /**Обе даты приходятся на один день.
                Начало в рабочее время, окончание после*/
                sd == ed && s.hour in schedule.firstHour..schedule.lastHour && e.hour > schedule.lastHour ->
                    ((schedule.lastHour - s.toLocalTime().hour) * 3600 + (60 - s.toLocalTime().minute) * 60).toLong()
                /**Обе даты приходятся на один день.
                Начало до рабочего времени, окончание в рабочее время*/
                sd == ed && s.hour < schedule.firstHour && e.hour in schedule.firstHour..schedule.lastHour ->
                    ((e.hour - schedule.firstHour) * 3600 + e.minute * 60 + e.second).toLong()
                /**Разные даты*/
                sd != ed && Duration.between(s, e).toMillis() == 0L -> 0
                sd != ed ->
                    Duration.between(s, e).toDays() * (schedule.lastHour - schedule.firstHour + 1) * 3600
                else -> {
                    println("Unhandled case ${a[index].id} $s // $e")
                    println()
                    0L
                }
            }
            a[index].timeSpent = (result / 60).toInt()
        }
        /** Результат возвращается в минутах */
        return a.sumBy { it.timeSpent ?: 0 } + agg;
    }
}
