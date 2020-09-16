package fsight.youtrack.db.models

import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime

data class DevOpsFeature(
    val id: Int,
    val priority: Int,
    val createdDate: Timestamp,
    val updatedDate: Timestamp,
    val assignee: String,
    val title: String
)

fun DevOpsFeature.toTableRow(): String {
    return """<tr>
            <td><p><a href='https://tfsprod.fsight.ru/Foresight/AP/_workitems/edit/${this.id}'>${this.title}</a></p></td>
            <td><p>${this.priority}</p></td>
            <td><p>${this.createdDate.toLocalDateTime().toLocalDate()} (${Duration.between(this.createdDate.toLocalDateTime(), LocalDateTime.now()).toDays()} дней)</p></td>
            <td><p>${this.updatedDate.toLocalDateTime().toLocalDate()} (${Duration.between(this.updatedDate.toLocalDateTime(), LocalDateTime.now()).toDays()} дней)</p></td>
        </tr>""".trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ").replace("> <", "><")
}
