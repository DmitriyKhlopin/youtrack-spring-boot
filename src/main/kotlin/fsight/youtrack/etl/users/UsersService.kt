package fsight.youtrack.etl.users

import fsight.youtrack.AUTH
import fsight.youtrack.generated.jooq.tables.Users.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UsersService(private val dslContext: DSLContext) {
    fun getUsers() {
        var skip = 0
        do {
            val body = UsersInterface.create().get(AUTH, skip).execute().body().orEmpty()
            body.forEach {
                val b = UserDetailsInterface.create().get(AUTH, it.login).execute().body()
                dslContext
                        .insertInto(USERS)
                        .set(USERS.USER_LOGIN, it.login)
                        .set(USERS.RING_ID, it.ringId)
                        .set(USERS.URL, it.url)
                        .set(USERS.EMAIL, b?.email ?: "")
                        .set(USERS.FULL_NAME, b?.fullName ?: "")
                        .onDuplicateKeyUpdate()
                        .set(USERS.USER_LOGIN, it.login)
                        .set(USERS.RING_ID, it.ringId)
                        .set(USERS.URL, it.url)
                        .set(USERS.EMAIL, b?.email ?: "")
                        .set(USERS.FULL_NAME, b?.fullName ?: "")
                        .execute()
            }
            skip += 10
        } while (body.isNotEmpty())
    }
}