package fsight.youtrack.etl

import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.YouTrackIssue
import org.springframework.http.ResponseEntity

interface IETL {
    fun loadDataFromYT(manual: Boolean, customFilter: String? = null, parameters: String = ""): ETLResult?
    fun getBundles()
    fun getUsers()
    fun getIssueById(id: String): YouTrackIssue
    fun getIssueHistory(idReadable: String)
    fun getTimelineById(idReadable: String):Any
}
