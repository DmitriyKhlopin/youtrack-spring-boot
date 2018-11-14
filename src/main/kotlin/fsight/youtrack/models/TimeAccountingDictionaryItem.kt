package fsight.youtrack.models

import com.google.gson.annotations.SerializedName

data class TimeAccountingDictionaryItem(
        /*@SerializedName("PROJ_SHORT_NAME")*/
        val projectShortName: String?,
        /*@SerializedName("CUSTOMER")*/
        val customer: String?,
        /*@SerializedName("PROJ_ETS")*/
        val projectEts: String?,
        /*@SerializedName("ITERATION_PATH")*/
        val iterationPath: String?
)