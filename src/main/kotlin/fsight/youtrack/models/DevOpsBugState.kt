package fsight.youtrack.models

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class DevOpsBugState(
        @SerializedName("System_Id")
        var systemId: String,
        @SerializedName("System_State")
        var state: String,
        @SerializedName("IterationPath")
        var sprint: String,
        var sprintDates: Pair<Timestamp, Timestamp>?,
        var stateOrder: Int = -1
)