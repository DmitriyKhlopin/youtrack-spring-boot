package fsight.youtrack.models.hooks

import java.util.*


data class HookResource(
        var id: Int? = null,
        var workItemId: Int? = null,
        var rev: Int? = null,
        var revisedBy: DevOpsUser? = null,
        var fields: HashMap<String, Any> = hashMapOf(),
        var revision: HookRevision? = null
)
