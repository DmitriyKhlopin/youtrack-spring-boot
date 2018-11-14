package fsight.youtrack.api.time

import fsight.youtrack.generated.jooq.tables.TimeAccounting.TIME_ACCOUNTING
import fsight.youtrack.models.TimeAccountingItem
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ModelAttribute
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class TimeAccountingImpl(private val dsl: DSLContext) : TimeAccountingService {
    override fun getWorkItemsToday(token: String?): List<TimeAccountingItem> {
        val ts = Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay())
        val q = dsl.select(
                TIME_ACCOUNTING.CRDATE.`as`("crDate"),
                TIME_ACCOUNTING.UNITS.`as`("units"),
                TIME_ACCOUNTING.AGENT.`as`("agent"),
                TIME_ACCOUNTING.CHANGEDDATE.`as`("changedDate"),
                TIME_ACCOUNTING.SERVER.`as`("server"),
                TIME_ACCOUNTING.PROJECTS.`as`("projects"),
                TIME_ACCOUNTING.TEAMPROJECT.`as`("teamProject"),
                TIME_ACCOUNTING.ID.`as`("id"),
                TIME_ACCOUNTING.DISCIPLINE.`as`("discipline"),
                TIME_ACCOUNTING.PERSON.`as`("person"),
                TIME_ACCOUNTING.WIT.`as`("wit"),
                TIME_ACCOUNTING.TITLE.`as`("title"),
                TIME_ACCOUNTING.ITERATIONPATH.`as`("iterationPath"),
                TIME_ACCOUNTING.ROLE.`as`("role"))
                .from(TIME_ACCOUNTING)
                .where(TIME_ACCOUNTING.CRDATE.eq(ts))
        return q.fetchInto(TimeAccountingItem::class.java)
                .sortedBy { it.agent }
    }

    override fun getWorkItemsYesterday(token: String?): List<TimeAccountingItem> {
        val ts = Timestamp.valueOf(LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay())
        val q = dsl.select(
                TIME_ACCOUNTING.CRDATE.`as`("crDate"),
                TIME_ACCOUNTING.UNITS.`as`("units"),
                TIME_ACCOUNTING.AGENT.`as`("agent"),
                TIME_ACCOUNTING.CHANGEDDATE.`as`("changedDate"),
                TIME_ACCOUNTING.SERVER.`as`("server"),
                TIME_ACCOUNTING.PROJECTS.`as`("projects"),
                TIME_ACCOUNTING.TEAMPROJECT.`as`("teamProject"),
                TIME_ACCOUNTING.ID.`as`("id"),
                TIME_ACCOUNTING.DISCIPLINE.`as`("discipline"),
                TIME_ACCOUNTING.PERSON.`as`("person"),
                TIME_ACCOUNTING.WIT.`as`("wit"),
                TIME_ACCOUNTING.TITLE.`as`("title"),
                TIME_ACCOUNTING.ITERATIONPATH.`as`("iterationPath"),
                TIME_ACCOUNTING.ROLE.`as`("role"))
                .from(TIME_ACCOUNTING)
                .where(TIME_ACCOUNTING.CRDATE.eq(ts))
        return q.fetchInto(TimeAccountingItem::class.java)
                .sortedBy { it.agent }
    }

    @ModelAttribute("items")
    override fun getWorkItemsBad(token: String?): List<TimeAccountingItem> = dsl.select(
            TIME_ACCOUNTING.CRDATE.`as`("crDate"),
            TIME_ACCOUNTING.UNITS.`as`("units"),
            TIME_ACCOUNTING.AGENT.`as`("agent"),
            TIME_ACCOUNTING.CHANGEDDATE.`as`("changedDate"),
            TIME_ACCOUNTING.SERVER.`as`("server"),
            TIME_ACCOUNTING.PROJECTS.`as`("projects"),
            TIME_ACCOUNTING.TEAMPROJECT.`as`("teamProject"),
            TIME_ACCOUNTING.ID.`as`("id"),
            TIME_ACCOUNTING.DISCIPLINE.`as`("discipline"),
            TIME_ACCOUNTING.PERSON.`as`("person"),
            TIME_ACCOUNTING.WIT.`as`("wit"),
            TIME_ACCOUNTING.TITLE.`as`("title"),
            TIME_ACCOUNTING.ITERATIONPATH.`as`("iterationPath"),
            TIME_ACCOUNTING.ROLE.`as`("role"))
            .from(TIME_ACCOUNTING)
            .fetchInto(TimeAccountingItem::class.java)
            .filter {
                it.crDate == null || it.units == 0 || it.agent == null || it.changedDate == null
                        || it.projects == "Value is null" || it.id == null || it.iterationPath == null
            }.sortedByDescending { it.crDate }
    /*return ResponseEntity.ok().body(Gson().toJson(result))*/

    override fun getWorkItems(dateFrom: String?, dateTo: String?): List<TimeAccountingItem> {
        val q = if (dateFrom != null && dateTo != null) {
            val df = LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val dt = LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            dsl.select(
                    TIME_ACCOUNTING.CRDATE.`as`("crDate"),
                    TIME_ACCOUNTING.UNITS.`as`("units"),
                    TIME_ACCOUNTING.AGENT.`as`("agent"),
                    TIME_ACCOUNTING.CHANGEDDATE.`as`("changedDate"),
                    TIME_ACCOUNTING.SERVER.`as`("server"),
                    TIME_ACCOUNTING.PROJECTS.`as`("projects"),
                    TIME_ACCOUNTING.TEAMPROJECT.`as`("teamProject"),
                    TIME_ACCOUNTING.ID.`as`("id"),
                    TIME_ACCOUNTING.DISCIPLINE.`as`("discipline"),
                    TIME_ACCOUNTING.PERSON.`as`("person"),
                    TIME_ACCOUNTING.WIT.`as`("wit"),
                    TIME_ACCOUNTING.TITLE.`as`("title"),
                    TIME_ACCOUNTING.ITERATIONPATH.`as`("iterationPath"),
                    TIME_ACCOUNTING.ROLE.`as`("role"))
                    .from(TIME_ACCOUNTING)
                    .where(TIME_ACCOUNTING.CRDATE.between(Timestamp.valueOf(df.atStartOfDay()), Timestamp.valueOf(dt.atStartOfDay())))

        } else {
            val ts = Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay())
            dsl.select(
                    TIME_ACCOUNTING.CRDATE.`as`("crDate"),
                    TIME_ACCOUNTING.UNITS.`as`("units"),
                    TIME_ACCOUNTING.AGENT.`as`("agent"),
                    TIME_ACCOUNTING.CHANGEDDATE.`as`("changedDate"),
                    TIME_ACCOUNTING.SERVER.`as`("server"),
                    TIME_ACCOUNTING.PROJECTS.`as`("projects"),
                    TIME_ACCOUNTING.TEAMPROJECT.`as`("teamProject"),
                    TIME_ACCOUNTING.ID.`as`("id"),
                    TIME_ACCOUNTING.DISCIPLINE.`as`("discipline"),
                    TIME_ACCOUNTING.PERSON.`as`("person"),
                    TIME_ACCOUNTING.WIT.`as`("wit"),
                    TIME_ACCOUNTING.TITLE.`as`("title"),
                    TIME_ACCOUNTING.ITERATIONPATH.`as`("iterationPath"),
                    TIME_ACCOUNTING.ROLE.`as`("role"))
                    .from(TIME_ACCOUNTING)
                    .where(TIME_ACCOUNTING.CRDATE.eq(ts))
        }
        return q.fetchInto(TimeAccountingItem::class.java)
                .sortedBy { it.agent }
    }
}











