package fsight.youtrack.models

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class TFSRequirement(
        @SerializedName("ID")
        val id: Int,
        @SerializedName("REV")
        val rev: Int,
        @SerializedName("STATE")
        val state: String? = null,
        @SerializedName("TYPE")
        val type: String? = null,
        @SerializedName("CREATE_DATE")
        val createDate: Timestamp,
        @SerializedName("SEVERITY")
        val severity: String? = null,
        @SerializedName("PROJECT")
        val project: String? = null,
        val customer: String? = null,
        @SerializedName("PROJECT_MANAGER")
        val productManager: String? = null,
        @SerializedName("PRODUCT_MANAGER_DIRECTOR")
        val productManagerDirector: String? = null,
        @SerializedName("PROPOSAL_QUALITY")
        val proposalQuality: String? = null,
        @SerializedName("PM_ACCEPTED")
        val pmAccepted: String? = null,
        @SerializedName("DM_ACCEPTED")
        val dmAccepted: String? = null,
        @SerializedName("PROJECT_NODE_NAME")
        val projectNodeName: String? = null,
        @SerializedName("PROJECT_PATH")
        val projectPath: String? = null,
        @SerializedName("AREA_NAME")
        val areaName: String? = null,
        @SerializedName("AREA_PATH")
        val areaPath: String? = null,
        @SerializedName("ITERATION_PATH")
        val iterationPath: String? = null,
        @SerializedName("ITERATION_NAME")
        val iterationName: String? = null,
        val title: String? = null,
        @SerializedName("PROBLEM_DESCRIPTION")
        val problemDescription: String? = null,
        @SerializedName("PROPOSED_CHANGE")
        val proposedChange: String? = null,
        @SerializedName("EXPECTED_RESULT")
        val expectedResult: String? = null
)