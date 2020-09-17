package fsight.youtrack.integrations.devops.hooks

import fsight.youtrack.models.DevOpsWorkItem
import fsight.youtrack.models.hooks.Hook
import org.springframework.http.ResponseEntity

interface ITFSHooks {
    fun getHook(limit: Int): ResponseEntity<Any>
    fun getPostableHooks(limit: Int): ResponseEntity<Any>
    fun postHook(body: Hook?): ResponseEntity<Any>
    fun postCommand(id: String?, command: String): ResponseEntity<Any>
    fun getInferredState(bugStates: List<DevOpsWorkItem>): String
    fun getInferredSprint(bugStates: List<DevOpsWorkItem>): String
    fun getIssuesByWIId(id: Int): List<String>
}
