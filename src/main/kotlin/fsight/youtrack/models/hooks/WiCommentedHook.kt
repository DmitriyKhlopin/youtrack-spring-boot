package fsight.youtrack.models.hooks

open class Hook

data class WiCommentedHook(
    var subscriptionId: String? = null,
    var notificationId: Int? = null,
    var id: String? = null,
    var eventType: String? = null,
    var publisher: String? = null,
    var resource: WiCommentedHookResource? = null,
    var message: HookMessage? = null,
    var detailedMessage: HookMessage? = null
) :Hook()

fun WiCommentedHook.getMentionedUsers(): List<String> =
    this.detailedMessage?.text?.split("(")?.map { it.substringBefore(")") }?.filter { it.startsWith("mailto:FS\\") }?.map { it.substringAfter("mailto:FS\\") } ?: listOf()
fun WiCommentedHook.getYtId(): String = this.resource?.revision?.fields?.get("System.Title").toString().trimStart().substringBefore(delimiter = " ").substringBefore(delimiter = ".")
fun WiCommentedHook.getDevOpsId(): Int? = this.resource?.workItemId
fun WiCommentedHook.getFieldValue(fieldName: String): Any? = this.resource?.revision?.fields?.get(fieldName)
fun WiCommentedHook.newSprint(): String = this.resource?.revision?.fields?.get("System.IterationPath").toString()
fun WiCommentedHook.isBug(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Bug"
fun WiCommentedHook.isFeature(): Boolean = this.resource?.revision?.fields?.get("System.WorkItemType").toString() == "Feature"
