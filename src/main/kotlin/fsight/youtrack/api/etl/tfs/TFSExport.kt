package fsight.youtrack.api.etl.tfs

import fsight.youtrack.models.TFSRequirement
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class TFSExport(private val dslContext: DSLContext) : ITFSExport {
    override fun getTFSItems(): List<TFSRequirement> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTFSItemsById(id: Int): List<TFSRequirement> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}