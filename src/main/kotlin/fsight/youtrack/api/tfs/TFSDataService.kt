package fsight.youtrack.api.tfs

import fsight.youtrack.models.TFSItem

interface TFSDataService {
    fun getItemsCount(): Int
    fun getItems(): List<TFSItem>
    fun getItemById(id: Int): TFSItem
    fun postItemToYouTrack(id: Int): TFSItem
}