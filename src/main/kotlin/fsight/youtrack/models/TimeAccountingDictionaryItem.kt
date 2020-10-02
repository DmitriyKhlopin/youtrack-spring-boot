package fsight.youtrack.models

import java.sql.Timestamp

data class TimeAccountingDictionaryItem(
    val id: Int?,
    val projectShortName: String?,
    val customer: String?,
    val projectEts: String?,
    val iterationPath: String?,
    val dateFrom: Timestamp?,
    val dateTo: Timestamp?,
    val isApproved: Boolean?
)

fun TimeAccountingDictionaryItem.isValid(): Boolean = projectShortName != null && customer != null && dateFrom != null && dateTo != null
