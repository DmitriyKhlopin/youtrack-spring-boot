package fsight.youtrack.models

import java.sql.Timestamp

data class TFSRequirement(
        val id: Int,
        val rev: Int,
        val state: String? = null,
        val type: String? = null,
        val createDate: Timestamp,
        val severity: String? = null,
        val project: String? = null,
        val customer: String? = null,
        val productManager: String? = null,
        val productManager_director: String? = null,
        val proposalQuality: String? = null,
        val pmAccepted: String? = null,
        val dmAccepted: String? = null,
        val projectNodeName: String? = null,
        val projectPath: String? = null,
        val areaName: String? = null,
        val areaPath: String? = null,
        val iterationPath: String? = null,
        val iterationName: String? = null,
        val title: String? = null,
        val problemDescription: String? = null,
        val proposedChange: String? = null,
        val expectedResult: String? = null
)