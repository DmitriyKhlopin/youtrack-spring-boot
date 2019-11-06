package fsight.youtrack.api.dictionaries

import fsight.youtrack.models.sql.DevOpsStateOrder

interface IDictionary {
    val commercialProjects: List<String>
    val innerProjects: List<String>
    val devOpsStates: List<DevOpsStateOrder>
    fun preloadCommercialProjects()
    fun preloadInnerProjects()
    fun preloadDevOpsStates()
}
