package fsight.youtrack.api.tfs

import fsight.youtrack.models.TFSWI

interface TFSDataService{
    fun getRequirements():List<TFSWI>
}