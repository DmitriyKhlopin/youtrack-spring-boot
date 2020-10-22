package fsight.youtrack.models.hooks

import java.util.*


data class WiUpdatedHookResource(
        var id: Int? = null,
        var workItemId: Int? = null,
        var rev: Int? = null,
        var revisedBy: DevOpsUser? = null,
        /*var fields: Any? = null,*/
        var fields: HashMap<String, HookFieldPair> = hashMapOf(),
        var revision: HookRevision? = null
)

data class HookFieldPair(
        var oldValue: Any? = null,
        var newValue: Any? = null
)
