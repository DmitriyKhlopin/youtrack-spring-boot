package fsight.youtrack.api.tfs.hooks

import fsight.youtrack.models.DevOpsBugState
import fsight.youtrack.models.hooks.Hook
import org.springframework.http.ResponseEntity
import java.sql.Timestamp

interface ITFSHooks {
    fun getHook(limit: Int): ResponseEntity<Any>
    fun getPostableHooks(limit: Int): ResponseEntity<Any>
    fun postHook(body: Hook?, bugs: List<Int>): ResponseEntity<Any>
    fun postCommand(id: String?, command: String, filter: String): ResponseEntity<Any>
    fun getComposedBugsState(ids: List<Int>): List<DevOpsBugState>
    fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, details: String?): Timestamp
}