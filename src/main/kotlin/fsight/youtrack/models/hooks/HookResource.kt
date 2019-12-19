package fsight.youtrack.models.hooks

import java.util.HashMap

data class HookResource(
        var id: Int? = null,
        var workItemId: Int? = null,
        var rev: Int? = null,
        var revisedBy: DevOpsUser? = null,
        var fields: HashMap<String, HookFieldPair> = hashMapOf(),
        var revision: HookRevision? = null
)