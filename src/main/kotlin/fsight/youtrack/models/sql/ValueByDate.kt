package fsight.youtrack.models.sql

import java.sql.Timestamp

data class ValueByDate(
    var date: Timestamp? = null,
    var value: Int? = null
)
