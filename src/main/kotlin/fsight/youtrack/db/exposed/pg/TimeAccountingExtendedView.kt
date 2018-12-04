package fsight.youtrack.db.exposed.pg

import org.jetbrains.exposed.sql.Table

object TimeAccountingExtendedView : Table(name = "time_accounting_extended") {
    val createDate = datetime(name = "crdate")
    val units = integer(name = "units")
    val agent = varchar(name = "agent", length = 128)
    val changedDate = datetime(name = "changeddate")
    val server = integer(name = "server")
    val projects = text(name = "projects")
    val teamProject = text(name = "teamproject")
    val id = varchar(name = "id", length = 256)
    val discipline = text(name = "discipline")
    val person = text(name = "person")
    val wit = text(name = "wit")
    val title = text(name = "title")
    val iterationPath = text(name = "iterationpath")
    val role = text(name = "role")
    val projectType = text("project_type")
    val ytProject = text("yt_project")
}

