package fsight.youtrack.models

import java.sql.Timestamp

data class ImportLogModel(val timestamp: Timestamp, val source: String, val table: String, val items: Int)