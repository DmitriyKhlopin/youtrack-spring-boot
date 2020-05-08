package fsight.youtrack.etl

import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.youtrack.Issue

interface IETL {
    fun runScheduledExport(): ETLResult?
    fun runManualExport(customFilter: String? = null, parameters: String = "", dateFrom: String? = null, dateTo: String? =null): Any?
    fun getBundles()
    fun getUsers()
    fun getIssueById(id: String): Issue
    fun getIssueHistory(idReadable: String)
    fun getTimelineById(idReadable: String): Any
}
