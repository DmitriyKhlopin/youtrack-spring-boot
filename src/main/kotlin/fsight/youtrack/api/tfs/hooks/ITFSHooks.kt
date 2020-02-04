package fsight.youtrack.api.tfs.hooks

import com.google.gson.JsonObject
import fsight.youtrack.models.DevOpsBugState
import fsight.youtrack.models.hooks.Hook
import org.springframework.http.ResponseEntity
import java.sql.Timestamp

interface ITFSHooks {
    fun getHook(limit: Int): ResponseEntity<Any>
    fun getPostableHooks(limit: Int): ResponseEntity<Any>
    fun postHook(body: Hook?, bugs: List<Int>): ResponseEntity<Any>
    fun postCommand(id: String?, command: String, filter: String): ResponseEntity<Any>
    fun getAssociatedBugsState(id: String): JsonObject?
    fun getDevOpsBugsState(ids: List<Int>): List<DevOpsBugState>
    fun mergeStates(devOpsBugStates: List<DevOpsBugState>, hook: Hook)
    fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?): Timestamp
}