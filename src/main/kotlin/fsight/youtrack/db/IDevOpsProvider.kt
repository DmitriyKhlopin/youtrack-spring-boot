package fsight.youtrack.db

import fsight.youtrack.api.issues.DevOpsBug
import fsight.youtrack.db.models.devops.DevOpsFeature
import fsight.youtrack.db.models.devops.DevOpsFieldValue
import fsight.youtrack.models.DevOpsWorkItem

interface IDevOpsProvider {
    fun getFeaturesByPlanningBoardStates(states: List<String>): List<DevOpsFeature>
    fun getDevOpsItemsByIds(ids: List<Int>): List<DevOpsWorkItem>
    fun getCustomFieldsValues(ids: List<Int>, fieldIds: List<Int>, columns: List<String>): List<DevOpsFieldValue>
    fun getDevOpsItemsByIdsAndType(ids: List<Int>, type: String): List<DevOpsBug>
}
