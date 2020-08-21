package fsight.youtrack.models

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class DevOpsWorkItem(
    @SerializedName("System_Id")
    var systemId: Int,
    @SerializedName("System_State")
    var state: String,
    @SerializedName("IterationPath")
    var sprint: String,
    var sprintDates: Pair<Timestamp, Timestamp>?,
    var stateOrder: Int = -1,
    @SerializedName("Microsoft_VSTS_Common_Priority")
    var priority: String? = null,
    @SerializedName("System_CreatedDate")
    var createdDate: Timestamp? = null,
    @SerializedName("System_AssignedTo")
    var assignee: String? = null,
    @SerializedName("System_WorkItemType")
    var type: String? = null,
    @SerializedName("AreaPath")
    var area: String? = null,
    @SerializedName("System_Title")
    var title: String? = null,
    @SerializedName("System_CreatedBy")
    var author: String? = null
)
