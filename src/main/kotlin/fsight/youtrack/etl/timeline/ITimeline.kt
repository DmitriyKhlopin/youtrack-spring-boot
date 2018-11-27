package fsight.youtrack.etl.timeline

interface ITimeline {
    fun launchCalculation()
    fun calculateForId(issueId: String)
}