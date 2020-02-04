package fsight.youtrack.models.youtrack


data class IssueLinkType(
        var name: String?,
        var localizedName: String?,
        var sourceToTarget: String?,
        var localizedSourceToTarget: String?,
        var targetToSource: String?,
        var localizedTargetToSource: String?,
        var directed: Boolean?,
        var aggregation: Boolean?,
        var readOnly: Boolean?
)