package fsight.youtrack.etl

import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.v2.Issue

interface IETL {
    fun loadDataFromYT(manual: Boolean, customFilter: String? = null, parameters: String = ""): ETLResult?
    fun getBundles()
    fun getUsers()
    fun getIssueById(id: String): Issue
}
