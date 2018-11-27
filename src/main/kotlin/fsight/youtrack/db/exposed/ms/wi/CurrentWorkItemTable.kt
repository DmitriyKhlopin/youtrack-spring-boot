package fsight.youtrack.db.exposed.ms.wi

import org.jetbrains.exposed.sql.Table

object CurrentWorkItemTable : Table(name = "CurrentWorkItemView") {
    val systemId = integer(name = "System_Id")
    val state = varchar(name = "System_State", length = 64)
    val previousState = varchar(name = "PreviousState", length = 64)
}