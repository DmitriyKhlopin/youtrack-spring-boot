package fsight.youtrack.etl.logs

import fsight.youtrack.generated.jooq.tables.ImportLog.IMPORT_LOG
import fsight.youtrack.models.ImportLogModel
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImportLog(private val dslContext: DSLContext) : IImportLog {
    override fun getLog(): ArrayList<ImportLogModel> {
        return arrayListOf()
    }

    override fun saveLog(model: ImportLogModel) {
        try {
            dslContext.insertInto(IMPORT_LOG)
                    .set(IMPORT_LOG.SOURCE_URL, model.source)
                    .set(IMPORT_LOG.DESTINATION_TABLE_NAME, model.table)
                    .set(IMPORT_LOG.ITEMS_COUNT, model.items)
                    .executeAsync()
        } catch (e: Exception) {
            println(e)
        }
    }
}