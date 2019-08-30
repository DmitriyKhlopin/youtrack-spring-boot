package fsight.youtrack.api.common

interface ICommon {
    fun findIssueInDB(id: String?): Boolean
}
