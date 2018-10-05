package fsight.youtrack.api.time

import fsight.youtrack.models.TimeAccountingItem

interface TimeAccountingService {
    fun getWorkItemsToday(token: String?): List<TimeAccountingItem>
    fun getWorkItemsYesterday(token: String?): List<TimeAccountingItem>
    fun getWorkItemsBad(token: String?): List<TimeAccountingItem>
}