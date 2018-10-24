package fsight.youtrack.etlV2.issues

import fsight.youtrack.models.v2.Issue

interface IssuesServiceV2 {
    fun getPaginated(size: Int)
    fun getById(id: String): Issue
}