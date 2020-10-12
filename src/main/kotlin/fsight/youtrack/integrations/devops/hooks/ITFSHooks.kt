package fsight.youtrack.integrations.devops.hooks

import fsight.youtrack.models.hooks.Hook
import org.springframework.http.ResponseEntity

interface ITFSHooks {
    fun handleWiUpdated(body: Hook?): ResponseEntity<Any>
    fun handleWiCommented(body: Hook?): ResponseEntity<Any>
    fun postCommand(id: String?, command: String): ResponseEntity<Any>

}
