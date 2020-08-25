package fsight.youtrack.models.sql

data class Issue(
    var id: String? = null,
    var type: String? = null,
    var priority: String? = null,
    var devOpsIssue: String? = null,
    var solutionAttempts: Int? = null,
    var firstResponseViolation: Int? = null,
    var firstResponseAgent: String? = null,
    var solutionViolation: Int? = null,
    var solutionAgent: String? = null,
    var evaluation: String? = null,
    var postponements: Int? = null,
    var requestedClarifications: Int? = null,
    var isCommercial: Boolean? = null
)
