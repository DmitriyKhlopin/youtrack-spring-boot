package fsight.youtrack.models

import java.sql.Timestamp

data class TFSWI(
        val id: Int,
        val rev: Int,
        val state: String? = null,
        val type: String? = null,
        val create_date: Timestamp,
        val severity: String? = null,
        val project: String? = null,
        val customer: String? = null,
        val product_manager: String? = null,
        val product_manager_director: String? = null,
        val proposal_quality: String? = null,
        val pm_accepted: String? = null,
        val dm_accepted: String? = null,
        val project_node_name: String? = null,
        val project_path: String? = null,
        val area_name: String? = null,
        val area_path: String? = null,
        val iteration_path: String? = null,
        val iteration_name: String? = null
)