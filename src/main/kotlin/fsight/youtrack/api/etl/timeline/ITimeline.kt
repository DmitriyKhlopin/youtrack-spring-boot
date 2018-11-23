package fsight.youtrack.api.etl.timeline

interface ITimeline {
    fun launchCalculation()
    fun calculateForId(issueId: String)
}