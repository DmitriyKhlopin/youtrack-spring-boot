package fsight.youtrack.etl.sync

interface ISync {
    fun getActiveIssuesYT(): List<Any>
}