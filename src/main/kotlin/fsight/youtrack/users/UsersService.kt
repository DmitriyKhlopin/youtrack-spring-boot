package fsight.youtrack.users

import fsight.youtrack.AUTH
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import youtrack.jooq.tables.Users

@Service
class UsersService(private val dslContext: DSLContext) {
    fun getUsers() {
        var skip = 0
        do {
            val body = UsersInterface.create().get(AUTH, skip).execute().body().orEmpty()
            body.forEach {
                val b = UserDetailsInterface.create().get(AUTH, it.login).execute().body()
                dslContext
                        .insertInto(Users.USERS)
                        .set(Users.USERS.USER_LOGIN, it.login)
                        .set(Users.USERS.RING_ID, it.ringId)
                        .set(Users.USERS.URL, it.url)
                        .set(Users.USERS.EMAIL, b?.email ?: "")
                        .set(Users.USERS.FULL_NAME, b?.fullName ?: "")
                        .onDuplicateKeyUpdate()
                        .set(Users.USERS.USER_LOGIN, it.login)
                        .set(Users.USERS.RING_ID, it.ringId)
                        .set(Users.USERS.URL, it.url)
                        .set(Users.USERS.EMAIL, b?.email ?: "")
                        .set(Users.USERS.FULL_NAME, b?.fullName ?: "")
                        .execute()
            }
            skip += 10
        } while (body.isNotEmpty())
    }
}