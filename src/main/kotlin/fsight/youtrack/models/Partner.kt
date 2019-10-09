package fsight.youtrack.models

import java.sql.Timestamp

data class Partner(
    var partnerName: String? = null,
    var projectId: String? = null,
    var customerName: String? = null,
    var issuesCount: Int? = null,
    var productVersions: String? = null,
    var databaseVersions: String? = null,
    var etsProject: String? = null,
    var iteration: String? = null,
    var dateTo: Timestamp? = null,
    var isCommercial: Boolean? = null,
    var isDesktop: Boolean? = null,
    var isWeb: Boolean? = null,
    var isMobile: Boolean? = null,
    var isDemo: Boolean? = null,
    var isImportant: Boolean? = null
)
