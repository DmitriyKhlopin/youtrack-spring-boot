package fsight.youtrack.etl.timeline

interface TimelineService {
    fun launchCalculation()
    fun calculateForId(issueId: String)
}