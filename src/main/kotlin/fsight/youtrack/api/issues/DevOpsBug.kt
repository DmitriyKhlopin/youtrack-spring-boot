package fsight.youtrack.api.issues

data class DevOpsBug1(
    val id: Int,
    val state: String? = null,
    val reason: String? = null,
    val lastUpdate: String? = null,
    val iteration: String? = null,
    val changedBy: String? = null,
    val responsible: String? = null,
    val resolvedReason: String? = null,
    val priority: Int,
    val foundIn: String? = null,
    val integratedIn: String? = null,
    val severity: String? = null,
    val area: String? = null,
    val title: String? = null,
    val triage: String? = null,
    val type: String? = null
)
