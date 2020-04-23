package fsight.youtrack.api.issues

data class DevOpsBug(
        val id: Int,
        val state: String,
        val reason: String,
        val lastUpdate: String,
        val iteration: String,
        val changedBy: String,
        val responsible: String,
        val resolvedReason: String? = null,
        val priority: Int,
        val foundIn: String,
        val integratedIn: String? = null,
        val severity: String,
        val area: String
)