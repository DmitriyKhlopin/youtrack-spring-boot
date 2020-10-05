package fsight.youtrack.db

import fsight.youtrack.ExposedTransformations
import fsight.youtrack.api.issues.DevOpsBug
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
        "AreaPath",
        "IterationPath",
        "Microsoft_VSTS_Build_FoundIn",
        "Microsoft_VSTS_Build_IntegrationBuild",
        "Microsoft_VSTS_Common_Priority",
        "Microsoft_VSTS_Common_ResolvedReason",
        "Microsoft_VSTS_Common_Severity",
        "Microsoft_VSTS_Common_Triage",
        "System_AssignedTo",
        "System_ChangedBy",
        "System_ChangedDate",
        "System_CreatedBy",
        "System_CreatedDate",
        "System_Id",
        "System_Reason",
        "System_State",
        "System_Title",
        "System_WorkItemType"
    )

    override fun getDevOpsItemsByIds(ids: List<Int>): List<DevOpsWorkItem> {
        if (ids.isEmpty()) return listOf()
        val statement =
            """select ${fields.joinToString(separator = ",")} from CurrentWorkItemView where System_Id in (${
                ids.joinToString(",")
            }) and TeamProjectCollectionSK = 37"""
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsWorkItem(it) }
    }

    override fun getDevOpsItemsByIdsAndType(ids: List<Int>, type: String): List<DevOpsBug> {
        if (ids.isEmpty()) return listOf()
        val statement = """
            SELECT ${fields.joinToString(separator = ",")}
            FROM CurrentWorkItemView
            WHERE
                System_Id IN (${ids.joinToString("','", prefix = "'", postfix = "'")})
                AND System_WorkItemType = '${type}'
                AND TeamProjectCollectionSK = 37
      """
        val a = statement.execAndMap(ms) { ExposedTransformations().toDevOpsBug(it) }
        return a.toList()
    }

    override fun getCustomFieldsValues(ids: List<Int>, fieldIds: List<Int>, columns: List<String>): List<DevOpsFieldValue> {
        if (ids.isEmpty() || fieldIds.isEmpty() || columns.isEmpty()) return listOf()
        val statement =
            "SELECT ${columns.joinToString(", ")} FROM [AzureDevOps_Foresight].[dbo].[vw_WorkItemCustomAll] where FieldId in (${fieldIds.joinToString(", ")}) and id in (${ids.joinToString(", ")})"
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsFieldValue(it) }
    }

    override fun getFeaturesByPlanningBoardStates(states: List<String>): List<DevOpsFeature> {
        val statement =
            "select ${fields.joinToString(separator = ",")} from CurrentWorkItemView where TeamProjectCollectionSK = 37 and System_WorkItemType = 'Feature' and AreaPath = '\\AP\\Technical Support' and System_BoardColumn in (${
                states.joinToString(separator = ",") { "'$it'" }
            })"
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsFeature(it) }
    }

}



