package fsight.youtrack.models.hooks

data class WiUpdatedHook(
    var subscriptionId: String? = null,
    var notificationId: Int? = null,
    var id: String? = null,
    var eventType: String? = null,
    var publisher: String? = null,
    var resource: WiUpdatedHookResource? = null
):Hook()

fun WiUpdatedHook.isFieldChanged(fieldName: String): Boolean = this.resource?.fields?.get(fieldName) != null
fun WiUpdatedHook.oldFieldValue(fieldName: String): Any? = this.resource?.fields?.get(fieldName)?.oldValue
fun WiUpdatedHook.newFieldValue(fieldName: String): Any? = this.resource?.fields?.get(fieldName)?.newValue
fun WiUpdatedHook.getYtId(): String = this.resource?.revision?.fields?.get("System.Title").toString().trimStart().substringBefore(delimiter = " ").substringBefore(delimiter = ".")
fun WiUpdatedHook.getDevOpsId(): Int? = this.resource?.workItemId
fun WiUpdatedHook.getFieldValue(fieldName: String): Any? = this.resource?.revision?.fields?.get(fieldName)
fun WiUpdatedHook.wasIncludedToSprint(): Boolean {
    val new = this.newFieldValue("System.IterationPath")
    val old = this.oldFieldValue("System.IterationPath")
    return old == "AP\\Backlog" && new != "AP\\Backlog"
}

fun WiUpdatedHook.wasExcludedFromSprint(): Boolean {
    val new = newFieldValue("System.IterationPath")
    val old = oldFieldValue("System.IterationPath")
    return old != "AP\\Backlog" && new == "AP\\Backlog"
}

fun WiUpdatedHook.sprintHasChanged(): Boolean = this.isFieldChanged("System.IterationPath")
fun WiUpdatedHook.newSprint(): String = this.resource?.revision?.fields?.get("System.IterationPath").toString()
fun WiUpdatedHook.stateHasChanged(): Boolean = true
fun WiUpdatedHook.isBug(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Bug"
fun WiUpdatedHook.isFeature(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Feature"
fun WiUpdatedHook.movedToSupportArea():Boolean = this.resource?.fields?.get("System.AreaPath")?.newValue == "AP\\Technical Support"

