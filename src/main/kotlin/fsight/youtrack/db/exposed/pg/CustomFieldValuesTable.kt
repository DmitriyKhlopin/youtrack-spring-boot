package fsight.youtrack.db.exposed.pg

import org.jetbrains.exposed.sql.Table

object CustomFieldValuesTable : Table(name = "custom_field_values") {
    val issueId = varchar(name = "issue_id", length = 64)
    val fieldName = varchar(name = "field_name", length = 256)
    val fieldValue = text(name = "field_value").nullable()
}