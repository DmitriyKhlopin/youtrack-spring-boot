package fsight.youtrack.integrations.devops.hooks

import fsight.youtrack.models.hooks.WiCommentedHook
import fsight.youtrack.models.hooks.WiUpdatedHook
import org.springframework.http.ResponseEntity

interface ITFSHooks {
    fun handleWiUpdated(body: WiUpdatedHook?): Any
    fun handleWiCommented(body: WiCommentedHook?): Any
    fun postCommand(id: String?, command: String): Any

}
