package fsight.youtrack.integrations.devops.features

interface IFeaturesAnalyzer {
    fun analyze(): Any
    fun analyzePendingFeatures(): Any
    fun analyzeRejectedFeatures(): Any
}
