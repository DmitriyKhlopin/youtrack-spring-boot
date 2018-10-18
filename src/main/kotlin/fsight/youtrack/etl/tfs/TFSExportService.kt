package fsight.youtrack.etl.tfs

import fsight.youtrack.models.TFSItem

interface TFSExportService {
    fun getTFSItems(): List<TFSItem>
    fun getTFSItemsById(id: Int): List<TFSItem>
}