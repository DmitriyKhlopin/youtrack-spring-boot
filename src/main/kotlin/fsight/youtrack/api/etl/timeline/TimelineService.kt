package fsight.youtrack.api.etl.timeline

interface TimelineService {
    fun launchCalculation()
    fun calculateForId(issueId: String)
}