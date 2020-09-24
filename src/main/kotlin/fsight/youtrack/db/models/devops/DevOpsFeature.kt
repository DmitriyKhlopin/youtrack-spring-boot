package fsight.youtrack.db.models.devops

import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime

data class DevOpsFeature(
    var ord: Int,
    var id: Int,
    var priority: Int,
    var createdDate: Timestamp,
    var updatedDate: Timestamp,
    var assignee: String,
    var title: String,
    var project: String,
    var author: String
)

fun DevOpsFeature.toTableRow(): String {
    return """<tr>
            <td><p>${this.ord}</p></td>            
            <td><p>${this.id}</p></td>            
            <td><p><a href='https://tfsprod.fsight.ru/Foresight/AP/_workitems/edit/${this.id}'>${this.title}</a></p></td>
            <td><p>${this.project}</p></td>
            <td><p>${this.priority}</p></td>
            <td><p>${this.createdDate.toLocalDateTime().toLocalDate()}<br>(${Duration.between(this.createdDate.toLocalDateTime(), LocalDateTime.now()).toDays()} дней)</p></td>
            <td><p>${this.updatedDate.toLocalDateTime().toLocalDate()}<br>(${Duration.between(this.updatedDate.toLocalDateTime(), LocalDateTime.now()).toDays()} дней)</p></td>
        </tr>""".trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ").replace("> <", "><")
}
