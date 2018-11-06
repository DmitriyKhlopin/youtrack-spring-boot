package fsight.youtrack.etl.users

import fsight.youtrack.AUTH
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.generated.jooq.tables.records.UsersRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UsersImplementation(private val dslContext: DSLContext) : UsersService {
    override fun getUsers() {
        val a = UserDetailsRetrofitInterface.create().get(AUTH)
        println(a.request().url())
        val b = a.execute()
        println("${b.code()} - ${b.errorBody()}")
        b?.body()?.forEach { println(it) }
        dslContext.deleteFrom(USERS).execute()
        val i = b?.body()?.map { item ->
            UsersRecord()
                    .setUserLogin(item.login)
                    .setRingId(item.ringId)
                    .setUrl("")
                    .setEmail(item.email)
                    .setFullName(item.fullName)
                    .setId(item.id)
        }
        dslContext
                .loadInto(USERS)
                .loadRecords(i)
                .fields(
                        USERS.USER_LOGIN,
                        USERS.RING_ID,
                        USERS.URL,
                        USERS.EMAIL,
                        USERS.FULL_NAME,
                        USERS.ID)
                .execute()
    }
}