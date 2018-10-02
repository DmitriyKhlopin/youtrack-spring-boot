package fsight.youtrack.dashboard

import fsight.youtrack.models.TimeAccountingItem

interface DashboardService {
    fun getWorkItemsToday(token: String?): List<TimeAccountingItem>
    fun getWorkItemsYesterday(token: String?): List<TimeAccountingItem>
    fun getWorkItemsBad(token: String?): List<TimeAccountingItem>
}