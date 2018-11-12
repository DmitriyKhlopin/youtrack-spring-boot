package fsight.youtrack.models

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class TFSTask(
        val id: Int,
        val rev: Int,
        val state: String? = null,
        val type: String? = null,
        @SerializedName("CREATE_DATE")
        val createDate: Timestamp,
        val discipline: String? = null,
        @SerializedName("DEVELOPMENT_EC")
        val developmentEc: Int,
        @SerializedName("TEST_EC")
        val testEc: Int,
        val project: String? = null,
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
        val description: String? = null,
        val developer: String? = null,
        val tester: String? = null
)