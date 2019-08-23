package fsight.youtrack.api.dictionaries

interface IDictionary {
    val commercialProjects: List<String>
    val innerProjects: List<String>
    fun preloadCommercialProjects()
    fun preloadInnerProjects()
}
