package fsight.youtrack.models

import com.google.gson.annotations.SerializedName

data class TFSDefect(
    @SerializedName("CHANGE_REQUEST_ID")
    var changeRequestId: Int,
    @SerializedName("DEFECT_ID")
    var parentId: Int,
    var parentType: String?,
    @SerializedName("MERGED_IN")
    var mergedIn: Int,
    @SerializedName("AREA_NAME")
    var areaName: String? = null,
    @SerializedName("AREA_PATH")
    var areaPath: String? = null,
    @SerializedName("ITERATION_PATH")
    var iterationPath: String? = null,
    @SerializedName("ITERATION_NAME")
    var iterationName: String? = null,
    @SerializedName("TITLE")
    var title: String? = null,
    @SerializedName("SYMPTOMS")
    var body: String? = null
)
