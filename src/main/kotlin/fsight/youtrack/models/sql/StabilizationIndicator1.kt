package fsight.youtrack.models.sql

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.gson.annotations.SerializedName

data class StabilizationIndicator1(
    val y: Int,
    val m: Int,
    val resultDaysDiff: Int,
    val resultIntervals: Int
)
