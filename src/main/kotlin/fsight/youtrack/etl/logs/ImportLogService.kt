package fsight.youtrack.etl.logs

import fsight.youtrack.models.ImportLogModel


interface ImportLogService {
    fun getLog(): ArrayList<ImportLogModel>
    fun saveLog(model: ImportLogModel)
}