package fsight.youtrack.api.issues

import fsight.youtrack.models.DevOpsWorkItem
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime

data class IssueWiThDetails(
    var id: String? = null,
    var project: String? = null,
    var customer: String? = null,
    var summary: String? = null,
    var created: Timestamp? = null,
    var priority: String? = null,
    var state: String? = null,
    var detailedState: String? = null,
    var type: String? = null,
    var assignee: String? = null,
    var comment: String? = null,
    var commentAuthor: String? = null,
    var issue: String? = null,
    var requirement: String? = null,
    var tfsPlainIssues: ArrayList<IssueTFSData> = arrayListOf(),
    var tfsData: ArrayList<Issues.TFSPlainIssue> = arrayListOf(),
    var devOpsBugs: ArrayList<DevOpsWorkItem> = arrayListOf(),
    var devOpsRequirements: ArrayList<DevOpsWorkItem> = arrayListOf(),
    /** Время хранится в секундах, в рабочем дне 9 часов*/
    var timeUser: Long? = null,
    var timeAgent: Long? = null,
    var timeDeveloper: Long? = null,
    var plainTags: String? = null,
    var team: String? = null,
    var firstResponseViolation: String? = null,
    var solutionViolation: String? = null,
    var tags: ArrayList<String> = arrayListOf()
)

fun IssueWiThDetails.toTableRow(index: Int): String {
    val d = Duration.between(this.created?.toLocalDateTime(), LocalDateTime.now()).toDays()
    return """<tr>
            <td><p>${index}</p></td>
            <td><p>${d}</p></td>
            <td><p><a href='https://support.fsight.ru/issue/${this.id}'>${this.summary}</a></p></td>         
            <td><p>${this.state}<br>${this.detailedState}</p></td>
            <td><p>${this.commentAuthor}</p></td>
            <td><p>${this.created?.toLocalDateTime()?.toLocalDate()}</p></td>
        </tr>""".trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ").replace("> <", "><")
}
