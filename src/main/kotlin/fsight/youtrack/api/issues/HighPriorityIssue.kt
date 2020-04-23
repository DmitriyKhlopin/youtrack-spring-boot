package fsight.youtrack.api.issues

import java.sql.Timestamp

data class HighPriorityIssue(
        var id: String? = null,
        var project: String? = null,
        var customer: String? = null,
        var summary: String? = null,
        var created: Timestamp? = null,
        var priority: String? = null,
        var state: String? = null,
        var type: String? = null,
        var assignee: String? = null,
        var comment: String? = null,
        var commentAuthor: String? = null,
        var issue: String? = null,
        var tfsPlainIssues: ArrayList<IssueTFSData> = arrayListOf(),
        var tfsData: ArrayList<Issues.TFSPlainIssue> = arrayListOf(),
        var devOpsBugs: ArrayList<DevOpsBug> = arrayListOf(),
        var timeUser: Long? = null,
        var timeAgent: Long? = null,
        var timeDeveloper: Long? = null
)