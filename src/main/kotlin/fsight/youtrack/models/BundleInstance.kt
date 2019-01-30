package fsight.youtrack.models

import fsight.youtrack.models.BundleValue

data class BundleInstance(
    val values: ArrayList<BundleValue>?,
    val name: String?,
    val id: String?,
    val `$type`: String?
)
