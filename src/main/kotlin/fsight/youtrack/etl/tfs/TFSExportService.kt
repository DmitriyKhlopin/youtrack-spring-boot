package fsight.youtrack.etl.tfs

import fsight.youtrack.models.TFSRequirement

interface TFSExportService {
    fun getTFSItems(): List<TFSRequirement>
    fun getTFSItemsById(id: Int): List<TFSRequirement>
}