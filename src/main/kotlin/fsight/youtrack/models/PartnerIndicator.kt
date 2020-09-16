package fsight.youtrack.models

import java.sql.Timestamp

data class PartnerIndicator(
    var partner: String?,
    var issueId: String?,
    var createdDate: Timestamp?,
    var resolvedDate: Timestamp?,
    var state: String?,
    var customer: String?,
    var ets: String?,
    var public: Boolean?,
    var type: String?,
    var priority: String?,
    var product: String?
)

