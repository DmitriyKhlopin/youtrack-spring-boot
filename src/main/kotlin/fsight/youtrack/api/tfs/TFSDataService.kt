package fsight.youtrack.api.tfs

import fsight.youtrack.models.TFSItem
import org.springframework.http.ResponseEntity

interface TFSDataService {
    fun getItemsCount(): Int
    fun getItems(offset: Int?, limit: Int?): ResponseEntity<Any>
    fun getItemById(id: Int): TFSItem
    fun postItemToYouTrack(id: Int): ResponseEntity<Any>
    fun toJson(id: Int): ResponseEntity<Any>
}