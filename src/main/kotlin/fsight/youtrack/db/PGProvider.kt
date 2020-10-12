package fsight.youtrack.db

import com.google.gson.Gson
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.EtsNames.ETS_NAMES
import fsight.youtrack.generated.jooq.tables.Hooks.HOOKS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldValuesRecord
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.splitToList
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class PGProvider(private val dsl: DSLContext) : IPGProvider {

    private val teamCustomField = CUSTOM_FIELD_VALUES.`as`("team")

    override fun saveHookToDatabase(
        body: Hook?,
        fieldState: String?,
        fieldDetailedState: String?,
        errorMessage: String?,
        inferredState: String?,
        commands: ArrayList<String>?,
        type: String,
        rule: ArrayList<Pair<String, Int>>?
    ): Timestamp {
        return dsl
            .insertInto(HOOKS)
            .set(HOOKS.RECORD_DATE_TIME, Timestamp.from(Instant.now()))
            .set(HOOKS.HOOK_BODY, Gson().toJson(body).toString())
            .set(HOOKS.FIELD_STATE, fieldState)
            .set(HOOKS.FIELD_DETAILED_STATE, fieldDetailedState)
            .set(HOOKS.ERROR_MESSAGE, errorMessage)
            .set(HOOKS.INFERRED_STATE, inferredState)
            .set(HOOKS.COMMANDS, commands?.joinToString(separator = " "))
            .set(HOOKS.TYPE, type)
            .returning(HOOKS.RECORD_DATE_TIME)
            .fetchOne().recordDateTime
    }

    override fun getDevOpsAssignees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            ETS_NAMES.ETS_NAME.`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")

        ).from(ETS_NAMES).fetchInto(ETSNameRecord::class.java)
    }

    override fun getSupportEmployees(): List<ETSNameRecord> {
        return dsl.select(
            ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
            ETS_NAMES.ETS_NAME.`as`("etsName"),
            ETS_NAMES.FULL_NAME.`as`("fullName"),
            ETS_NAMES.SUPPORT.`as`("isSupport")

        ).from(ETS_NAMES).where(ETS_NAMES.SUPPORT).fetchInto(ETSNameRecord::class.java)
    }

    override fun getIssueIdsByWIId(id: Int): List<String> {
        return dsl
            .select(CUSTOM_FIELD_VALUES.ISSUE_ID, CUSTOM_FIELD_VALUES.FIELD_VALUE)
            .from(CUSTOM_FIELD_VALUES)
            .where(
                CUSTOM_FIELD_VALUES.FIELD_NAME.`in`(listOf("Issue", "Requirement"))
                    .and(CUSTOM_FIELD_VALUES.FIELD_VALUE.like("%%$id%%"))
            )
            .fetchInto(CustomFieldValuesRecord::class.java)
            .filter { it.fieldValue.replace(" ", "").splitToList().contains(id.toString()) }
            .map { it.issueId }
    }

    override fun getIssuesIdsInDevelopment(): List<String> {
        return dsl
            .select(CUSTOM_FIELD_VALUES.ISSUE_ID)
            .from(CUSTOM_FIELD_VALUES)
            .leftJoin(teamCustomField).on(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(teamCustomField.ISSUE_ID).and(teamCustomField.FIELD_VALUE.eq("Команда")))
            .where(CUSTOM_FIELD_VALUES.FIELD_NAME.`in`(listOf("State")).and(CUSTOM_FIELD_VALUES.FIELD_VALUE.eq("Направлена разработчику")))
            .and(teamCustomField.FIELD_VALUE.isNull)
            /*.limit(10)*/
            .fetchInto(CustomFieldValuesRecord::class.java)
            .map { it.issueId }
    }
}
