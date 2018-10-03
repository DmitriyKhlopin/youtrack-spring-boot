package fsight.youtrack.etl

import fsight.youtrack.models.ETLResult

interface ETLService {
    fun loadDataFromYT(): ETLResult
}