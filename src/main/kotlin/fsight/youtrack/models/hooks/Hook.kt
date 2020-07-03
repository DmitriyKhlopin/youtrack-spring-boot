package fsight.youtrack.models.hooks


data class Hook(
    var subscriptionId: String? = null,
    var notificationId: Int? = null,
    var id: String? = null,
    var eventType: String? = null,
    var publisher: String? = null,
    var resource: HookResource? = null
) {
    fun isFieldChanged(fieldName: String): Boolean = this.resource?.fields?.get(fieldName) != null
    fun oldFieldValue(fieldName: String): Any? = this.resource?.fields?.get(fieldName)?.oldValue
    fun newFieldValue(fieldName: String): Any? = this.resource?.fields?.get(fieldName)?.newValue
    fun getYtId(): String = this.resource?.revision?.fields?.get("System.Title").toString().trimStart().substringBefore(delimiter = " ").substringBefore(delimiter = ".")
    fun getDevOpsId(): Int? = this.resource?.workItemId
    fun getFieldValue(fieldName: String): Any? = this.resource?.revision?.fields?.get(fieldName)
    fun wasIncludedToSprint(): Boolean {
        val new = newFieldValue("System.IterationPath")
        val old = oldFieldValue("System.IterationPath")
        return old == "AP\\Backlog" && new != "AP\\Backlog"
    }

    fun wasExcludedFromSprint(): Boolean {
        val new = newFieldValue("System.IterationPath")
        val old = oldFieldValue("System.IterationPath")
        return old != "AP\\Backlog" && new == "AP\\Backlog"
    }

    fun sprintHasChanged(): Boolean = true
    fun stateHasChanged(): Boolean = true
    fun isBug(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Bug"
    fun isFeature(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Feature"
}


