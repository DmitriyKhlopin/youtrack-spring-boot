package fsight.youtrack.db

import fsight.youtrack.ExposedTransformations
import fsight.youtrack.db.models.devops.DevOpsFeature
import fsight.youtrack.db.models.devops.DevOpsFieldValue
import fsight.youtrack.execAndMap
import fsight.youtrack.models.DevOpsWorkItem
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class DevOpsProvider(@Qualifier("tfsDataSource") private val ms: Database) : IDevOpsProvider {
    val fields = listOf(
        "System_Id",
        "System_State",
        "IterationPath",
        "Microsoft_VSTS_Common_Priority",
        "System_WorkItemType",
        "System_CreatedDate",
        "System_ChangedDate",
        "System_AssignedTo",
        "System_Title",
        "System_CreatedBy",
        "AreaPath"
    )

    override fun getFeaturesByPlanningBoardStates(states: List<String>): List<DevOpsFeature> {
        val statement =
            "select ${fields.joinToString(separator = ",")} from CurrentWorkItemView where TeamProjectCollectionSK = 37 and System_WorkItemType = 'Feature' and AreaPath = '\\AP\\Technical Support' and System_BoardColumn in (${
                states.joinToString(separator = ",") { "'$it'" }
            })"
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsFeature(it) }
    }

    override fun getDevOpsWiState(ids: List<Int>): List<DevOpsWorkItem> {
        if (ids.isEmpty()) return listOf()
        val statement =
            """select ${fields.joinToString(separator = ",")} from CurrentWorkItemView where System_Id in (${
                ids.joinToString(",")
            }) and TeamProjectCollectionSK = 37"""
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsWorkItem(it) }
    }

    override fun getCustomFieldsValues(ids: List<Int>, fieldIds: List<Int>, columns: List<String>): List<DevOpsFieldValue> {
        if (ids.isEmpty() || fieldIds.isEmpty() || columns.isEmpty()) return listOf()
        val statement =
            "SELECT ${columns.joinToString(", ")} FROM [AzureDevOps_Foresight].[dbo].[vw_WorkItemCustomAll] where FieldId in (${fieldIds.joinToString(", ")}) and id in (${ids.joinToString(", ")})"
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsFieldValue(it) }
    }
}
