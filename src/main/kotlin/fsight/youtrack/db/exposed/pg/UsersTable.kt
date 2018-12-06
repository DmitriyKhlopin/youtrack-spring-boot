package fsight.youtrack.db.exposed.pg

import org.jetbrains.exposed.sql.Table

object UsersTable : Table(name = "users") {
    val userLogin = varchar(name = "user_login", length = 255)
    val ringId = varchar(name = "ring_id", length = 255)
    val url = varchar(name = "url", length = 1024)
    val email = varchar(name = "email", length = 255)
    val fullName = varchar(name = "full_name", length = 255)
    val id = varchar(name = "id", length = 64)
}