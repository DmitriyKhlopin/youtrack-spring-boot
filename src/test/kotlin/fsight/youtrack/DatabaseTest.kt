package fsight.youtrack

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.db.exposed.helper.Queries
import fsight.youtrack.models.DevOpsBugState
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils
import java.io.File
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

    private val bugIds = listOf(21604, 16684)

    init {
        test1()
        getBugsByIdsQueryTest()
    }

    @Test
    fun test1() {
        val ids = listOf(21604, 16684)
        val result = Queries().getBugsByIdsQuery(bugIds).execAndMap(tfsConnection) { ExposedTransformations().toDevOpsState(it) }
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

    @Test
    fun getBugsByIdsQueryTest() {
        val file: File = ResourceUtils.getFile("classpath:test/data.json")
        assert(file.exists())
        val bugsFromJson: List<DevOpsBugState> = Gson().fromJson(String(file.readBytes()), object : TypeToken<List<DevOpsBugState>>() {}.type)


        val bugsFromDatabase = Queries().getBugsByIdsQuery(bugIds).execAndMap(tfsConnection) { ExposedTransformations().toDevOpsState(it) }
        assertEquals(bugsFromJson, bugsFromDatabase, "List re not equal")
        assert(true)
    }
}