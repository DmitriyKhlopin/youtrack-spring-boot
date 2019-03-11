package fsight.youtrack.api.time

import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.DictionaryProjectCustomerEts.DICTIONARY_PROJECT_CUSTOMER_ETS
import fsight.youtrack.generated.jooq.tables.FactWork.FACT_WORK
import fsight.youtrack.generated.jooq.tables.TimeAccounting.TIME_ACCOUNTING
import fsight.youtrack.models.FactWorkItem
import fsight.youtrack.models.TimeAccountingDictionaryItem
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
class TimeAccounting(private val dsl: DSLContext) : ITimeAccounting {
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
            TIME_ACCOUNTING.ROLE.`as`("role")
        )
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
            TIME_ACCOUNTING.ROLE.`as`("role")
        )
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
        TIME_ACCOUNTING.ROLE.`as`("role")
    )
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
                TIME_ACCOUNTING.ROLE.`as`("role"),
                CUSTOM_FIELD_VALUES.FIELD_VALUE.`as`("priority")
            )
                .from(TIME_ACCOUNTING)
                .leftJoin(CUSTOM_FIELD_VALUES).on(
                    TIME_ACCOUNTING.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(
                        CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Priority")
                    )
                )
                .where(
                    TIME_ACCOUNTING.CRDATE.between(
                        Timestamp.valueOf(df.atStartOfDay()),
                        Timestamp.valueOf(dt.atStartOfDay())
                    )
                )

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
                TIME_ACCOUNTING.ROLE.`as`("role"),
                CUSTOM_FIELD_VALUES.FIELD_VALUE.`as`("priority")
            )
                .from(TIME_ACCOUNTING)
                .leftJoin(CUSTOM_FIELD_VALUES).on(
                    TIME_ACCOUNTING.ID.eq(CUSTOM_FIELD_VALUES.ISSUE_ID).and(
                        CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Priority")
                    )
                )
                .where(TIME_ACCOUNTING.CRDATE.eq(ts))
        }
        return q.fetchInto(TimeAccountingItem::class.java)
            .sortedBy { it.agent }
    }

    override fun getDictionary(): List<TimeAccountingDictionaryItem> {
        return dsl.select(
            DICTIONARY_PROJECT_CUSTOMER_ETS.PROJ_SHORT_NAME.`as`("projectShortName"),
            DICTIONARY_PROJECT_CUSTOMER_ETS.CUSTOMER.`as`("customer"),
            DICTIONARY_PROJECT_CUSTOMER_ETS.PROJ_ETS.`as`("projectEts"),
            DICTIONARY_PROJECT_CUSTOMER_ETS.ITERATION_PATH.`as`("iterationPath")
        )
            .from(DICTIONARY_PROJECT_CUSTOMER_ETS)
            .fetchInto(TimeAccountingDictionaryItem::class.java)
    }

    /**
     * Данные с портала загружаются с помощью OBJ771674 в схеме DM*/
    override fun getFactWork(emails: String?, dateFrom: String?, dateTo: String?): List<FactWorkItem> {
        val e = emails?.split(",") ?: listOf("dmitriy.khlopin@fsight.ru")
        val q = if (dateFrom != null && dateTo != null) {
            val df = LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val dt = LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            dsl.select(
                FACT_WORK.EMAIL.`as`("user"),
                FACT_WORK.FACT_DATE.`as`("date"),
                FACT_WORK.PLANNED_TIME.`as`("plannedTime"),
                FACT_WORK.FACT_TIME.`as`("spentTime"),
                FACT_WORK.FACT_TU.`as`("accountedTime")
            )
                .from(FACT_WORK)
                .where(
                    FACT_WORK.FACT_DATE.between(
                        Timestamp.valueOf(df.atStartOfDay()),
                        Timestamp.valueOf(dt.atStartOfDay())
                    )
                )
        } else {
            val ts = Timestamp.valueOf(LocalDateTime.now().toLocalDate().atStartOfDay())
            dsl.select(
                FACT_WORK.EMAIL.`as`("user"),
                FACT_WORK.FACT_DATE.`as`("date"),
                FACT_WORK.PLANNED_TIME.`as`("plannedTime"),
                FACT_WORK.FACT_TIME.`as`("spentTime"),
                FACT_WORK.FACT_TU.`as`("accountedTime")
            )
                .from(FACT_WORK)
                .where(FACT_WORK.FACT_DATE.eq(ts))
        }
        println(q.sql.toString())
        return q.and(FACT_WORK.EMAIL.`in`(e)).fetchInto(FactWorkItem::class.java).sortedBy { it.user }
    }
}











