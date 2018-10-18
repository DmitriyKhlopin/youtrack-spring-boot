package fsight.youtrack.etl.tfs

import fsight.youtrack.models.TFSItem
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class TFSExportImplementation(private val dslContext: DSLContext) : TFSExportService {
    override fun getTFSItems(): List<TFSItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTFSItemsById(id: Int): List<TFSItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}