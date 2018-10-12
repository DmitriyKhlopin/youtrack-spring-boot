package fsight.youtrack.etl

import fsight.youtrack.ETLState
import fsight.youtrack.models.ETLResult

interface ETLService {
    fun getCurrentState(): ETLState
    fun loadDataFromYT(manual: Boolean, customFilter: String?): ETLResult?
}