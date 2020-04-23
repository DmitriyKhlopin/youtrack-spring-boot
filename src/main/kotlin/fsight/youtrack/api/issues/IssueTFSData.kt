package fsight.youtrack.api.issues

data class IssueTFSData(
        var issueId: Int? = null,
        var issueState: String? = null,
        var issueMergedIn: Int? = null,
        var issueReason: String? = null,
        var issueLastUpdate: String? = null,
        var issueIterationPath: String? = null,
var defectId: Int? = null,
var defectState: String? = null,
var defectReason: String? = null,
var defectIterationPath: String? = null,
var defectDevelopmentManager: String? = null,
var defectDeadline: String? = null,
var changeRequestId: Int? = null,
var changeRequestMergedIn: String? = null,
var iterationPath: String? = null,
var changeRequestReason: String? = null
)