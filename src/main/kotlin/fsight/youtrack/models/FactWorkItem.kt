package fsight.youtrack.models

import java.sql.Timestamp

data class FactWorkItem(
        var user: String? = null,
        var date: Timestamp? = null,
        var spentTime: Int? = null,
        var accountedTime: Int? = null
)