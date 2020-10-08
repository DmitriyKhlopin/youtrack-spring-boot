package fsight.youtrack.models.hooks

import com.google.gson.internal.LinkedTreeMap


data class Hook(
    var subscriptionId: String? = null,
    var notificationId: Int? = null,
    var id: String? = null,
    var eventType: String? = null,
    var publisher: String? = null,
    var resource: HookResource? = null,
    var message: HookMessage? = null,
    var detailedMessage: HookMessage? = null
) {
    fun isFieldChanged(fieldName: String): Boolean = this.resource?.fields?.get(fieldName) != null
    fun oldFieldValue(fieldName: String): Any? {
        this.resource?.fields?.get(fieldName).also { return if (it is LinkedTreeMap<*, *>) it["oldValue"] else it.toString() }
    }

    fun newFieldValue(fieldName: String): Any? {
        this.resource?.fields?.get(fieldName).also { return if (it is LinkedTreeMap<*, *>) it["newValue"] else it.toString() }
    }

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

    fun sprintHasChanged(): Boolean = this.resource?.fields?.get("System.IterationPath") != null
    fun newSprint(): String = this.resource?.revision?.fields?.get("System.IterationPath").toString()
    fun stateHasChanged(): Boolean = true
    fun isBug(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Bug"
    fun isFeature(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Feature"
    fun getMentionedUsers(): List<String> = this.detailedMessage?.text?.split("(")?.map { it.substringBefore(")") }?.filter { it.startsWith("mailto:FS\\") }?.map { it.substringAfter("mailto:FS\\") } ?: listOf()
}


