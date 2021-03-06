package fsight.youtrack.api.users

import fsight.youtrack.generated.jooq.tables.EtsNames.ETS_NAMES
import fsight.youtrack.models.UserDetails
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Users(private val dsl: DSLContext) : IUsers {
    override fun getAll(): List<UserDetails> {
        return dsl.select(
                ETS_NAMES.FSIGHT_EMAIL.`as`("email"),
                ETS_NAMES.FULL_NAME.`as`("fullName")
        ).from(ETS_NAMES).where(ETS_NAMES.SUPPORT.eq(true)).fetchInto(UserDetails::class.java)
    }
}
