package fsight.youtrack.etl.users

import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.generated.jooq.tables.UserGroup.USER_GROUP
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.generated.jooq.tables.records.UserGroupRecord
import fsight.youtrack.models.toUserRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UsersETL(private val dslContext: DSLContext) : IUsersETL {
    override fun getUsers() {
        val response = YouTrackAPI.create(Converter.GSON).getUserDetails(AUTH).execute()
        println("Loading users")
        val users = response.body()?.users?.map { it.toUserRecord() }
        println("Loaded ${users?.size} users")
        dslContext.deleteFrom(USERS).execute()
        val stored = dslContext
            .loadInto(USERS)
            .loadRecords(users)
            .fields(
                USERS.USER_LOGIN,
                USERS.RING_ID,
                USERS.URL,
                USERS.EMAIL,
                USERS.FULL_NAME,
                USERS.ID,
                USERS.CREATION_TIME,
                USERS.LAST_ACCESS_TIME,
                USERS.IS_BANNED
            )
            .execute()
            .stored()
        println("Stored $stored users")
        val groups = arrayListOf<UserGroupRecord>()
        response.body()?.users?.forEach { user ->
            user.groups?.forEach { hubUserGroup ->
                groups.add(UserGroupRecord().setType(hubUserGroup.type).setName(hubUserGroup.name).setEmail(user.profile?.email?.email))
            }
        }
        dslContext.deleteFrom(USER_GROUP).execute()
        val storedGroups = dslContext
            .loadInto(USER_GROUP)
            .loadRecords(groups)
            .fields(
                USER_GROUP.TYPE,
                USER_GROUP.NAME,
                USER_GROUP.EMAIL
            ).execute().stored()
        println("Loaded ${users?.size} users and ${groups.size} group bindings. Stored $stored users and $storedGroups group bindings.")
    }
}
