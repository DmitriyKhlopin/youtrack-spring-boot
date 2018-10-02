package fsight.youtrack.dashboard

import fsight.youtrack.models.TimeAccountingItem
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ModelAttribute
import youtrack.jooq.tables.TimeAccounting.TIME_ACCOUNTING
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
@Transactional
class DashboardImpl(private val dsl: DSLContext) : DashboardService {
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
}











