package fsight.youtrack.db.exposed.pg

import org.jetbrains.exposed.sql.Table

object ProjectDictionaryTable : Table(name = "dictionary_project_customer_ets") {
    val projectShortName = varchar(name = "proj_short_name", length = 64)
    val customer = varchar(name = "customer", length = 128)
    val projectEts = varchar(name = "proj_ets", length = 128)
    val iterationPath = varchar(name = "iteration_path", length = 128)
    val dateFrom = datetime(name = "date_from")
    val dateTo = datetime(name = "date_to")
}
