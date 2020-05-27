package fsight.youtrack.api.projects

data class ProjectWorkflow(
    val projects: List<String> = listOf(),
    val workflowId: String? = null
)