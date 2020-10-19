package fsight.youtrack.models

import java.sql.Timestamp

data class Dynamics(var week: Timestamp, var active: Int, var created: Int, var resolved: Int)
