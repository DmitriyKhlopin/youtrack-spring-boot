package fsight.youtrack

import fsight.youtrack.models.DevOpsBugState
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.Timestamp


class DatabaseTest {
    private val tfsConnection: Database by lazy {
        Database.connect(
                url = TFS_URL,
                driver = TFS_DRIVER,
                user = TFS_USER,
                password = TFS_PASSWORD
        )
    }

    init {
        test1()
    }

    @Test
    fun test1() {
        val ids = listOf(1, 2)
        val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${ids.joinToString(",")}) and TeamProjectCollectionSK = 37"""
        val result = statement.execAndMap(tfsConnection) { ExposedTransformations().toDevOpsState(it) }
        println(result)
        /*.map { d ->
            d.sprintDates = dictionaries.sprints[d.sprint]
            d.stateOrder = dictionaries.devOpsStates.firstOrNull { k -> k.state == d.state }?.order
                    ?: -1
            d
        }*/
        val state1 = DevOpsBugState(
                systemId = "1",
                state = "State",
                sprint = "1",
                sprintDates = Pair(Timestamp(1), Timestamp(1)),
                stateOrder = 1
        )

        val state2 = DevOpsBugState(
                systemId = "1",
                state = "State",
                sprint = "1",
                sprintDates = Pair(Timestamp(1), Timestamp(1)),
                stateOrder = 1
        )
        assertEquals(state1, state2, "States are not equal")
    }
}