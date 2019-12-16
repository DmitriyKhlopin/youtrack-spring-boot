package fsight.youtrack.api.tfs

import fsight.youtrack.models.TFSRequirement
import org.springframework.http.ResponseEntity

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
}
