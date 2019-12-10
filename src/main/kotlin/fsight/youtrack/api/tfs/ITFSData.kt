package fsight.youtrack.api.tfs

import com.google.gson.JsonObject
import fsight.youtrack.models.TFSRequirement

import org.springframework.http.ResponseEntity
import java.sql.Timestamp

interface ITFSData {
    fun getItemsCount(): Int
    fun getItems(offset: Int?, limit: Int?): ResponseEntity<Any>
    fun getItemById(id: Int): TFSRequirement
    fun getIterations(): ResponseEntity<Any>
    fun getBuildsByIteration(iteration: String): ResponseEntity<Any>
    fun getDefectsByFixedBuildId(iteration: String, build: String): ResponseEntity<Any>
    fun postChangeRequestById(id: Int, body: String?): ResponseEntity<Any>
    fun postItemToYouTrack(id: Int): ResponseEntity<Any>
    fun postItemsToYouTrack(offset: Int?, limit: Int?): ResponseEntity<Any>
    fun postItemsToYouTrack(iteration: String?): ResponseEntity<Any>
    fun toJson(id: Int): ResponseEntity<Any>
    fun initDictionaries()
    fun getHook(limit: Int): ResponseEntity<Any>
    fun getPostableHooks(limit: Int): ResponseEntity<Any>
    fun postHook(body: TFSData.Hook?): ResponseEntity<Any>
    fun postCommand(id: String?, command: String, filter: String): ResponseEntity<Any>
    fun getAssociatedBugsState(id: String): JsonObject?
    fun saveHookToDatabase(body: TFSData.Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?): Timestamp
}
