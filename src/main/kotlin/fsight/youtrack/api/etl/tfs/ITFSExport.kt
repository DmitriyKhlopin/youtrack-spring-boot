package fsight.youtrack.api.etl.tfs

import fsight.youtrack.models.TFSRequirement

interface ITFSExport {
    fun getTFSItems(): List<TFSRequirement>
    fun getTFSItemsById(id: Int): List<TFSRequirement>
}