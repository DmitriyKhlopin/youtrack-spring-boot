package fsight.youtrack.api.etl

import fsight.youtrack.ETLState
import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.v2.Issue

interface ETLService {
    fun getCurrentState(): ETLState
    fun loadDataFromYT(manual: Boolean, customFilter: String?): ETLResult?
    fun getBundles()
    fun getUsers()
    fun getIssueById(id: String): Issue
}