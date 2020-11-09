package fsight.youtrack.models

import java.sql.Timestamp

data class Velocity(
    var week: Timestamp?,
    var type: String?,
    var result: Int?
)

