package fsight.youtrack.models.hooks

import java.util.HashMap

data class HookRevision(
        var id: Int? = null,
        var rev: Int? = null,
        var fields: HashMap<String, Any>
)