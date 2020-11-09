package fsight.youtrack.models

import java.sql.Timestamp

data class VelocityAggregated(
    var week: Timestamp?,
    var all: Int?,
    var bugs: Int?,
    var features: Int?,
    var consultations: Int?
)
