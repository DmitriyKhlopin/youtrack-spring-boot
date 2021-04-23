package fsight.youtrack.models.sql

data class StabilizationIndicator1(
    val y: Int,
    val m: Int,
    val resultDaysDiff: Int,
    val resultIntervals: Int,
    val successfullyComplete: Float,
    val failed: Float,
    val lowRisk: Float,
    val highRisk: Float
)
