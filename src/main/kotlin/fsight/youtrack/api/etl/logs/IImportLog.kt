package fsight.youtrack.api.etl.logs

import fsight.youtrack.models.ImportLogModel


interface IImportLog {
    fun getLog(): ArrayList<ImportLogModel>
    fun saveLog(model: ImportLogModel)
}