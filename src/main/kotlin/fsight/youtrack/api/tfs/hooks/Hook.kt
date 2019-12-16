package fsight.youtrack.api.tfs.hooks

import java.util.*


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


    fun newFieldValue(fieldName: String): Any? {
        return this.resource?.fields?.get(fieldName)?.newValue
    }

    fun getYtId(): String = this.resource?.revision?.fields?.get("System.Title").toString().trimStart().substringBefore(delimiter = " ").substringBefore(delimiter = ".")

}


data class HookResource(
        var id: Int? = null,
        var workItemId: Int? = null,
        var rev: Int? = null,
        var revisedBy: DevOpsUser? = null,
        var fields: HashMap<String, HookFieldPair> = hashMapOf(),
        var revision: HookRevision? = null
)

data class DevOpsUser(
        var id: String? = null,
        var name: String? = null,
        var displayName: String? = null,
        var uniqueName: String? = null
)

data class HookFieldPair(
        var oldValue: Any? = null,
        var newValue: Any? = null
)

data class HookRevision(
        var id: Int? = null,
        var rev: Int? = null,
        var fields: HashMap<String, Any>
)