package fsight.youtrack

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.db.exposed.helper.Queries
import fsight.youtrack.models.DevOpsWorkItem
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils
import java.io.File


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

    @Test
    fun test1() {
        val result = Queries().getBugsByIdsQuery(bugIds).execAndMap(tfsConnection) { ExposedTransformations().toDevOpsWorkItem(it) }
        /*.map { d ->
            d.sprintDates = dictionaries.sprints[d.sprint]
            d.stateOrder = dictionaries.devOpsStates.firstOrNull { k -> k.state == d.state }?.order
                    ?: -1
            d
        }*/
        println(result)
        val state1 = DevOpsWorkItem(
                systemId = 16684,
                state = "Closed",
                sprint = "\\AP\\Backlog\\Q3 FY19\\Sprint 6",
                sprintDates = null,
                stateOrder = -1
        )

        val state2 = DevOpsWorkItem(
                systemId = 21604,
                state = "Closed",
                sprint = "\\AP\\Backlog\\Q3 FY19\\Sprint 7",
                sprintDates = null,
                stateOrder = -1
        )
        assertNotEquals(state1, state2, "States are not equal")
        assertEquals(listOf(state1, state2), result, "Bug lists are not equal")
    }

    @Test
    fun getBugsByIdsQueryTest() {
        val file: File = ResourceUtils.getFile("classpath:test/data.json")
        assert(file.exists())
        val bugsFromJson: List<DevOpsWorkItem> = Gson().fromJson(String(file.readBytes()), object : TypeToken<List<DevOpsWorkItem>>() {}.type)
        val bugsFromDatabase = Queries().getBugsByIdsQuery(bugIds).execAndMap(tfsConnection) { ExposedTransformations().toDevOpsWorkItem(it) }
        assertEquals(bugsFromJson, bugsFromDatabase, "List re not equal")
    }
}