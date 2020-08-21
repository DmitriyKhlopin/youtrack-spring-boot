package fsight.youtrack.integrations.devops.hooks

import com.google.gson.JsonObject
import fsight.youtrack.models.DevOpsWorkItem
import fsight.youtrack.models.hooks.Hook
import org.springframework.http.ResponseEntity
import java.sql.Timestamp

interface ITFSHooks {
    fun getHook(limit: Int): ResponseEntity<Any>
    fun getPostableHooks(limit: Int): ResponseEntity<Any>
    fun postHook(body: Hook?): ResponseEntity<Any>
    fun postCommand(id: String?, command: String): ResponseEntity<Any>
    fun getAssociatedBugsState(id: String): JsonObject?
    fun getDevOpsBugsState(ids: List<Int>): List<DevOpsWorkItem>
    fun getInferredState(bugStates: List<DevOpsWorkItem>): String
    fun getInferredSprint(bugStates: List<DevOpsWorkItem>): String
    fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, inferredState: String?): Timestamp
    fun getIssuesByWIId(id: Int): List<String>
}