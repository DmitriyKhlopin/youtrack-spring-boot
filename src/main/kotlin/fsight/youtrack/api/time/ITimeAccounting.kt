package fsight.youtrack.api.time

import fsight.youtrack.models.FactWorkItem
import fsight.youtrack.models.TimeAccountingDictionaryItem
import fsight.youtrack.models.TimeAccountingItem

interface ITimeAccounting {
    fun getWorkItemsToday(token: String?): List<TimeAccountingItem>
    fun getWorkItemsYesterday(token: String?): List<TimeAccountingItem>
    fun getWorkItemsBad(token: String?): List<TimeAccountingItem>
    fun getWorkItems(dateFrom: String?, dateTo: String?): List<TimeAccountingItem>
    fun getDictionary(): List<TimeAccountingDictionaryItem>
    fun getFactWork(emails: String?, dateFrom: String?, dateTo: String?): List<FactWorkItem>
}