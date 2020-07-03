package fsight.youtrack.api.tfs.hooks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.*
import fsight.youtrack.api.dictionaries.Dictionary
import fsight.youtrack.etl.ETLState
import fsight.youtrack.etl.issues.Issue
import fsight.youtrack.etl.logs.ImportLog
import fsight.youtrack.etl.projects.Projects
import fsight.youtrack.etl.timeline.Timeline
import fsight.youtrack.models.hooks.Hook
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils
import java.io.File

internal class TFSHooksTest {
    private val tfsConnection: Database by lazy {
        Database.connect(url = TFS_URL, driver = TFS_DRIVER, user = TFS_USER, password = TFS_PASSWORD)
    }

    private val db: DSLContext by lazy {
        DSL.using("jdbc:postgresql://v-ctp-postgres.fs.fsight.world:5432/youtrack?autoReconnect=true", "postgres", "Qwerty1")
    }

    @Test
    fun includedToSprint() {
        val bugs = listOf<Int>()
        val issueService = Issue(db, ImportLog(db), Timeline(db), ETLState())
        val dictionaryService = Dictionary(db)
        /*val hooksService = TFSHooks(*//*db, tfsConnection, issueService, dictionaryService*//*)*/
        val hooksService = TFSHooks(db, tfsConnection/*, issueService, dictionaryService*/)
        val file: File = ResourceUtils.getFile("classpath:test/hooks/includedToSprint.json")
        assert(file.exists())
        val body: Hook = Gson().fromJson(String(file.readBytes()), object : TypeToken<Hook>() {}.type)
        assertTrue(body.wasIncludedToSprint(), "Bug was not included to sprint")
        val ytId = body.getYtId()
        assertEquals("TEST-2", ytId, "YT ids are not equal")
        val reason = when {
            body.isFieldChanged("System.State") && body.wasIncludedToSprint() -> "State and sprint were changed"
            body.isFieldChanged("System.State") -> "State was changed"
            body.wasIncludedToSprint() -> "Sprint was changed"
            else -> return
        }
        assertEquals("Sprint was changed", reason, "Reasons are not equal")
        val actualIssueState = issueService.search("#$ytId", listOf("idReadable", "customFields(name,value(name))")).firstOrNull()
        assertNotNull(actualIssueState)
        val actualIssueFieldState = actualIssueState?.unwrapFieldValue("State")
        assertEquals("Направлена разработчику", actualIssueFieldState, "YT states are not equal")
        if (actualIssueFieldState != "Направлена разработчику") return
        val linkedBugs = if (bugs.isEmpty()) actualIssueState.unwrapFieldValue("Issue").toString().split(",", " ").mapNotNull { it.toIntOrNull() } else bugs
        val bugStates = hooksService.getDevOpsBugsState(linkedBugs).map {
            if (it.systemId == body.resource?.workItemId) {
                it.state = body.getFieldValue("System.State").toString()
                it.sprint = body.getFieldValue("System.IterationPath").toString()
            }
            it
        }.map {
            it.sprintDates = dictionaryService.sprints[it.sprint]
            it.stateOrder = dictionaryService.devOpsStates.firstOrNull { k -> k.state == it.state }?.order ?: -1
            it
        }
        val inferredState = when {
            bugStates.any { it.sprint == "\\AP\\Backlog" && it.state == "Proposed" } -> "Backlog"
            bugStates.any { it.state == "Proposed" } -> "Proposed"
            bugStates.all { it.state == "Closed" } -> "Closed"
            bugStates.all { it.state == "Closed" || it.state == "Resolved" } -> "Resolved"
            else -> bugStates.filter { it.state != "Closed" }.minBy { it.stateOrder }?.state ?: "Closed"
        }
        assertEquals("Proposed", inferredState, "Incorrect inferred state")
        assertFalse(body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved"))
        assertTrue(inferredState !in arrayOf("Closed", "Resolved") && (body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath")))
    }

    @Test
    fun excludedFromSprint() {
        val bugs = listOf<Int>()
        val issueService = Issue(db, ImportLog(db), Timeline(db), ETLState())
        val dictionaryService = Dictionary(db)
        val hooksService = TFSHooks(db, tfsConnection)
        val file: File = ResourceUtils.getFile("classpath:test/hooks/excludedFromSprint.json")
        assert(file.exists())
        val body: Hook = Gson().fromJson(String(file.readBytes()), object : TypeToken<Hook>() {}.type)
        assertTrue(body.wasExcludedFromSprint(), "Bug was not excluded sprint")
        val ytId = body.getYtId()
        assertEquals("TEST-3", ytId, "YT ids are not equal")
        assertTrue(body.wasExcludedFromSprint(), "Bug was not excluded from sprint")
        val actualIssueState = issueService.search("#$ytId", listOf("idReadable", "customFields(name,value(name))")).firstOrNull()
        assertNotNull(actualIssueState)
        val actualIssueFieldState = actualIssueState?.unwrapFieldValue("State")
        assertEquals("Направлена разработчику", actualIssueFieldState, "YT states are not equal")
        if (actualIssueFieldState != "Направлена разработчику") return
        val linkedBugs = if (bugs.isEmpty()) actualIssueState.unwrapFieldValue("Issue").toString().split(",", " ").mapNotNull { it.toIntOrNull() } else bugs
        val bugStates = hooksService.getDevOpsBugsState(linkedBugs).map {
            if (it.systemId == body.resource?.workItemId) {
                it.state = body.getFieldValue("System.State").toString()
                it.sprint = body.getFieldValue("System.IterationPath").toString()
            }
            it.sprintDates = dictionaryService.sprints[it.sprint]
            it.stateOrder = dictionaryService.devOpsStates.firstOrNull { k -> k.state == it.state }?.order ?: -1
            it
        }
        val inferredState = when {
            bugStates.any { it.sprint == "\\AP\\Backlog" && it.state == "Proposed" } -> "Backlog"
            bugStates.any { it.state == "Proposed" } -> "Proposed"
            bugStates.all { it.state == "Closed" } -> "Closed"
            bugStates.all { it.state == "Closed" || it.state == "Resolved" } -> "Resolved"
            else -> bugStates.filter { it.state != "Closed" }.minBy { it.stateOrder }?.state ?: "Closed"
        }
        assertEquals("Proposed", inferredState, "Incorrect inferred state")
        assertTrue(inferredState !in arrayOf("Closed", "Resolved") && (body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath")))
    }


    @Test
    fun activeToResolved() {
        val oldValue = "Active"
        val newValue = "Resolved"
        val bugs = listOf<Int>()
        val issueService = Issue(db, ImportLog(db), Timeline(db), ETLState())
        val projectsService = Projects(db)
        val dictionaryService = Dictionary(db)
        val hooksService = TFSHooks(db, tfsConnection/*, issueService, dictionaryService*/)
        val file: File = ResourceUtils.getFile("classpath:test/hooks/activeToResolved.json")
        assert(file.exists())
        val body: Hook = Gson().fromJson(String(file.readBytes()), object : TypeToken<Hook>() {}.type)
        assertTrue(body.oldFieldValue("System.State") == oldValue, "Previous state is not \"$oldValue\"")
        assertTrue(body.newFieldValue("System.State") == newValue, "New state is not \"$newValue\"")
        val ytId = body.getYtId()
        assertEquals("TEST-11", ytId, "YT ids are not equal")
        val actualIssueState = issueService.search("#$ytId", listOf("idReadable", "customFields(name,value(name))")).firstOrNull()
        assertNotNull(actualIssueState)
        val actualIssueFieldState = actualIssueState?.unwrapFieldValue("State")
        assertEquals("Направлена разработчику", actualIssueFieldState, "YT states are not equal")
        if (actualIssueFieldState != "Направлена разработчику") return
        val linkedBugs = if (bugs.isEmpty()) actualIssueState.unwrapFieldValue("Issue").toString().split(",", " ").mapNotNull { it.toIntOrNull() } else bugs
        val bugStates = hooksService.getDevOpsBugsState(linkedBugs).mergeWithHookData(body, dictionaryService.devOpsStates)
        val inferredState = hooksService.getInferredState(bugStates)
        assertEquals("Resolved", inferredState)
    }

    @Test
    fun wiTypeParseTest() {
        val file: File = ResourceUtils.getFile("classpath:test/hooks/wiType.json")
        assert(file.exists())
        val body: Hook = Gson().fromJson(String(file.readBytes()), object : TypeToken<Hook>() {}.type)
        assertTrue(body.isBug(), "This is not a \"Bug\"")
    }

    @Test
    fun getIssuesByWIId() {
        val hooksService = TFSHooks(db, tfsConnection)
        val issues = hooksService.getIssuesByWIId(60000)
        val expected = listOf("TEST-12", "TEST-13")
        assertThat(issues.containsAll(expected))
        assertEquals(expected.size, issues.size, "Lists have different length")
    }

    @Test
    fun getDevOpsBugsState() {
        val issueService = Issue(db, ImportLog(db), Timeline(db), ETLState())
        val dictionaryService = Dictionary(db)
        val issues = listOf("TEST-12", "TEST-13")
        val actualIssues = issueService.search(issues.joinToString(separator = " ") { "#$it" }, listOf("idReadable", "customFields(name,value(name))"))
        val hooksService = TFSHooks(db, tfsConnection)
        val file: File = ResourceUtils.getFile("classpath:test/hooks/wiType.json")
        assert(file.exists())
        val hook: Hook = Gson().fromJson(String(file.readBytes()), object : TypeToken<Hook>() {}.type)
        val a = hooksService.getDevOpsBugsState(actualIssues.getBugsAndFeatures()).mergeWithHookData(hook, dictionaryService.devOpsStates)
        a.forEach { println(it) }
        assertEquals(3, a.size, "Wrong number of work items")
    }
}