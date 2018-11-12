package fsight.youtrack.models

import java.sql.Timestamp

data class TFSTask(
        val id: Int,
        val rev: Int,
        val state: String? = null,
        val type: String? = null,
        val createDate: Timestamp,
        val discipline: String? = null,
        val developmentEc: Int,
        val testEc: Int,
        val project: String? = null,
        val projectNodeName: String? = null,
        val projectPath: String? = null,
        val areaName: String? = null,
        val areaPath: String? = null,
        val iterationPath: String? = null,
        val iterationName: String? = null,
        val title: String? = null,
        val description: String? = null,
        val developer: String? = null,
        val tester: String? = null
)