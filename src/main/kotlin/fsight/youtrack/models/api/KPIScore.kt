package fsight.youtrack.models.api

data class KPIScore(
        val issueId: String,
        val agent: String,
        val percentage: Float,
        val total: Float,
        val issueType: Float,
        val priority: Float,
        val level: Float,
        val solution: Float,
        val sla: Float,
        val evaluation: Float,
        val postponements: Float,
        val clarifications: Float,
        val violations: Int
)