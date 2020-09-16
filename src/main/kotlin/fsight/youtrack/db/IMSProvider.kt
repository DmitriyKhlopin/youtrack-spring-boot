package fsight.youtrack.db

import fsight.youtrack.db.models.DevOpsFeature

interface IMSProvider {
    fun getPendingFeatures(): List<DevOpsFeature>
}
