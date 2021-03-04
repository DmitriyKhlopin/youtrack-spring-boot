package fsight.youtrack.etl.timeline

import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.models.IssueTimelineItem
import fsight.youtrack.models.Schedule
import fsight.youtrack.toEndOfDate
import fsight.youtrack.toStartOfDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate

@Service
class Timeline : ITimeline {
    @Autowired
    private lateinit var pg: IPGProvider;

    @Autowired
    private lateinit var dictionaries: IDictionary

    private val schedule = Schedule("Стандартный", 1, 5, 11, 19)
    private val holidays: ArrayList<LocalDate> = arrayListOf()
    private val extraWorkDays: ArrayList<LocalDate> = arrayListOf()

    override fun updateDictionaryValues() {
        holidays.clear()
        extraWorkDays.clear()
        holidays.addAll(dictionaries.holidays)
        extraWorkDays.addAll(dictionaries.extraWorkDays)
    }

    override fun launchCalculation() {
        updateDictionaryValues()
        val i: List<String> = pg.getUnresolvedIssuesForTimelineCalculation()
        println("Need to calculate timelines for ${i.size} items.")
        val size = i.size
        i.asSequence().forEachIndexed { index, id ->
            calculateStateByIssueId(id)
            calculateDetailedStateByIssueId(id)
            print("Calculated timeline for ${index * 100 / size}% of issues\r")
        }
        println("Calculated timeline for 100% of issues")
        updateAllIssuesSpentTime()
    }

    override fun launchCalculationForPeriod(dateFrom: String?, dateTo: String?) {
        updateDictionaryValues()
        val df = dateFrom?.toStartOfDate() ?: return
        val dt = dateTo?.toStartOfDate() ?: return
        val i: List<String> = pg.getIssuesUpdatedInPeriod(df, dt)
        val size = i.size
        println("\nNeed to calculate timelines for $size items.")
        i.asSequence().forEachIndexed { index, s ->
            calculateStateByIssueId(s)
            print("Calculated timeline for ${index * 100 / size}% of issues\r")
        }
        updateAllIssuesSpentTime()
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
        println("\nNeed to calculate detailed timelines for $size items.")
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
            for (n in 0..j) {
                val df = dateFrom.plusDays(n).toLocalDate()
                when {
                    df in holidays -> {
                    }
                    df in extraWorkDays -> agg += (schedule.lastHour - schedule.firstHour + 1) * 60
                    df.dayOfWeek.value !in schedule.firstDay..schedule.lastDay -> {
                    }
                    else -> agg += (schedule.lastHour - schedule.firstHour + 1) * 60
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

    override fun updateAllIssuesSpentTime() {
        pg.updateAllIssuesSpentTime()
    }
}
