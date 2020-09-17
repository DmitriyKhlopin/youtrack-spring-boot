package fsight.youtrack.db

import com.google.gson.Gson
import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.generated.jooq.tables.EtsNames.ETS_NAMES
import fsight.youtrack.generated.jooq.tables.Hooks
import fsight.youtrack.models.hooks.Hook
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class PGProvider(private val dsl: DSLContext) : IPGProvider {
    override fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, inferredState: String?): Timestamp {
        return dsl
            .insertInto(Hooks.HOOKS)
            .set(Hooks.HOOKS.RECORD_DATE_TIME, Timestamp.from(Instant.now()))
            .set(Hooks.HOOKS.HOOK_BODY, Gson().toJson(body).toString())
            .set(Hooks.HOOKS.FIELD_STATE, fieldState)
            .set(Hooks.HOOKS.FIELD_DETAILED_STATE, fieldDetailedState)
            .set(Hooks.HOOKS.ERROR_MESSAGE, errorMessage)
            .set(Hooks.HOOKS.INFERRED_STATE, inferredState)
            .returning(Hooks.HOOKS.RECORD_DATE_TIME)
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
}
