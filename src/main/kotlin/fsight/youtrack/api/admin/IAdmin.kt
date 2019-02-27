package fsight.youtrack.api.admin

import java.sql.Timestamp

interface IAdmin {
    fun addBuild(name: String, date: Timestamp): Boolean
}
