package fsight.youtrack.integrations.devops.revisions

import fsight.youtrack.models.DevOpsWorkItem

interface IDevOpsRevisions {
    fun startRevision()
    fun checkBugs(): List<Int>
    fun getActiveBugsAndFeatures(stage: Int?, limit: Int?, offset: Int?): List<Any>
    fun getDevOpsWorkItems(ids: List<Int>): List<DevOpsWorkItem>
}