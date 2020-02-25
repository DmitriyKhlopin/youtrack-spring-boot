package fsight.youtrack.api.test

import fsight.youtrack.ETLState
import fsight.youtrack.ExposedTransformations
import fsight.youtrack.etl.IETLState
import fsight.youtrack.execAndMap
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class CustomTest(private val etlStateService: IETLState) : ICustomTest {
    private var counter = 0
    override fun increment(): Int {
        return ++counter
    }

    override fun getState(): ETLState {
        return etlStateService.state
    }

    data class Response(
            val order: Int,
            val timeInMillis: Long,
            val query: String
    )
}

