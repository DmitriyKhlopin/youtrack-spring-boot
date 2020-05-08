package fsight.youtrack.api.dictionaries

import fsight.youtrack.models.BundleValue
import fsight.youtrack.models.YouTrackProject
import fsight.youtrack.models.YouTrackUser
import fsight.youtrack.models.sql.DevOpsStateOrder
import java.sql.Timestamp
import java.util.*

interface IDictionary {
    val projects: List<YouTrackProject>
    val commercialProjects: List<String>
    val innerProjects: List<String>
    val devOpsStates: List<DevOpsStateOrder>
    val priorities: HashMap<String, String>
    val areas: HashMap<String, String>
    val buildPrefixes: HashMap<String, String>
    val buildSuffixes: HashMap<String, String>
    val users: List<YouTrackUser>
    val customFieldValues: List<BundleValue>
    val sprints: HashMap<String, Pair<Timestamp, Timestamp>>
    fun preloadCommercialProjects()
    fun preloadInnerProjects()
    fun preloadDevOpsStates()
    fun getTags(): List<String>
}
