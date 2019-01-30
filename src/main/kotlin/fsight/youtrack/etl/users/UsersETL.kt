package fsight.youtrack.etl.users

import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.generated.jooq.tables.UserGroup.USER_GROUP
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.generated.jooq.tables.records.UserGroupRecord
import fsight.youtrack.generated.jooq.tables.records.UsersRecord
import fsight.youtrack.models.HubUserGroup
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UsersETL(private val dslContext: DSLContext) : IUsersETL {


    override fun getUsers() {
        val response = YouTrackAPI.create(Converter.GSON).getUserDetails(AUTH).execute()
        val users = response.body()?.users?.map { item ->
            UsersRecord()
                .setUserLogin(item.login)
                .setRingId(item.id)
                .setUrl("")
                .setEmail(item.profile?.email?.email)
                .setFullName(item.name)
                .setId(item.id)
        }
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
                USERS.ID
            )
            .execute().stored()
        val groups = arrayListOf<HubUserGroup>()
        response.body()?.users?.forEach { user ->
            user.groups?.map { hubUserGroup ->
                hubUserGroup.email = user.profile?.email?.email
                hubUserGroup
            }.orEmpty().also { groups.addAll(it) }

        }
        val groupsMap = groups.map { hubUserGroup ->
            UserGroupRecord().setType(hubUserGroup.type).setName(hubUserGroup.name).setEmail(hubUserGroup.email)
        }

        dslContext.deleteFrom(USER_GROUP).execute()
        val storedGroups = dslContext
            .loadInto(USER_GROUP)
            .loadRecords(groupsMap)
            .fields(
                USER_GROUP.TYPE,
                USER_GROUP.NAME,
                USER_GROUP.EMAIL
            ).execute().stored()
        println("Loaded ${users?.size} users and ${groups.size} group bindings. Stored $stored users and $storedGroups group bindings.")
    }
}
