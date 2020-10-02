package fsight.youtrack.api.issues

data class IssueFilter(
    val priorities: List<String> = listOf(),
    val tags: List<String> = listOf(),
    val allTags: Boolean = false,
    val states: List<String> = listOf(),
    val detailedStates: List<String> = listOf(),
    val projects: List<String> = listOf(),
    val types: List<String> = listOf(),
    val customers: List<String> = listOf(),
    val limit: Int = Int.MAX_VALUE,
    var dateFrom: String? = null,
    var dateTo: String? = null
)
