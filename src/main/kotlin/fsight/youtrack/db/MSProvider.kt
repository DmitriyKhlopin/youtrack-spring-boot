package fsight.youtrack.db

import fsight.youtrack.ExposedTransformations
import fsight.youtrack.db.models.DevOpsFeature
import fsight.youtrack.execAndMap
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class MSProvider(@Qualifier("tfsDataSource") private val ms: Database) : IMSProvider {
    override fun getPendingFeatures(): List<DevOpsFeature> {
        val fields = listOf(
            "System_Id",
            "Microsoft_VSTS_Common_Priority",
            "System_CreatedDate",
            "System_ChangedDate",
            "System_AssignedTo",
            "System_Title"
        )
        val statement =
            "select ${fields.joinToString(separator = ",")} from CurrentWorkItemView where TeamProjectCollectionSK = 37 and System_WorkItemType = 'Feature' and AreaPath = '\\AP\\Technical Support' and System_BoardColumn = 'На рассмотрении РО'"
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsFeature(it) }
    }
}
