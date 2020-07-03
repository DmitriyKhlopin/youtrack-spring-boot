package fsight.youtrack.api.tfs.hooks

import com.google.gson.JsonObject
import fsight.youtrack.models.DevOpsBugState
import fsight.youtrack.models.hooks.Hook
import org.springframework.http.ResponseEntity
import java.sql.Timestamp

interface ITFSHooks {
    fun getHook(limit: Int): ResponseEntity<Any>
    fun getPostableHooks(limit: Int): ResponseEntity<Any>
    fun postHook(body: Hook?): ResponseEntity<Any>
    fun postCommand(id: String?, command: String): ResponseEntity<Any>
    fun getAssociatedBugsState(id: String): JsonObject?
    fun getDevOpsBugsState(ids: List<Int>): List<DevOpsBugState>
    fun getInferredState(bugStates: List<DevOpsBugState>): String
    fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, inferredState: String?): Timestamp
    fun getIssuesByWIId(id: Int): List<String>
}