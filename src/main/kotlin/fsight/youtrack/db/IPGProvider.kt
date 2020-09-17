package fsight.youtrack.db

import fsight.youtrack.db.models.pg.ETSNameRecord
import fsight.youtrack.models.hooks.Hook
import java.sql.Timestamp

interface IPGProvider {
    fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, inferredState: String?): Timestamp
    fun getDevOpsAssignees(): List<ETSNameRecord>
    fun getSupportEmployees():List<ETSNameRecord>
}
