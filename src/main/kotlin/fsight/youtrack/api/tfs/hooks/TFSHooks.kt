package fsight.youtrack.api.tfs.hooks

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import fsight.youtrack.DEVOPS_AUTH
import fsight.youtrack.ExposedTransformations
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.execAndMap
import fsight.youtrack.generated.jooq.tables.CustomFieldValues
import fsight.youtrack.generated.jooq.tables.Hooks
import fsight.youtrack.models.DevOpsBugState
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.models.youtrack.Command
import fsight.youtrack.models.youtrack.Issue
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.jooq.tools.json.JSONObject
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class TFSHooks(private val dsl: DSLContext,
               @Qualifier("tfsDataSource") private val ms: Database,
               private val issueService: IIssue,
               private val dictionaries: IDictionary
) : ITFSHooks {
    override fun getHook(limit: Int): ResponseEntity<Any> {
        val i = dsl
                .select(Hooks.HOOKS.HOOK_BODY)
                .from(Hooks.HOOKS)
                .orderBy(Hooks.HOOKS.RECORD_DATE_TIME.desc())
                .limit(limit)
                .fetchInto(String::class.java)
                .map { Gson().fromJson(it, Hook::class.java) }
        return ResponseEntity.ok(i)
    }

    override fun getPostableHooks(limit: Int): ResponseEntity<Any> {
        val i = dsl
                .select(Hooks.HOOKS.HOOK_BODY)
                .from(Hooks.HOOKS)
                .orderBy(Hooks.HOOKS.RECORD_DATE_TIME.desc())
                .limit(limit)
                .fetchInto(String::class.java)
                .map { Gson().fromJson(it, Hook::class.java) }
                .filter { it.resource?.fields?.get("System.State") != null }
                .map { it.resource?.fields?.get("System.State") }
        return ResponseEntity.ok(i)
    }

    override fun postCommand(id: String?, command: String, filter: String): ResponseEntity<Any> {
        val cmd = Gson().toJson(Command(issues = arrayListOf(Issue(idReadable = id)), query = command))
        val response = YouTrackAPI.create().postCommand(DEVOPS_AUTH, cmd).execute()
        return ResponseEntity.ok("Issue $id returned response code ${response.code()} on command: $command")
    }

    override fun postHook(body: Hook?, bugs: List<Int>): ResponseEntity<Any> {
        return try {
            if (body?.isFieldChanged("System.State") != true) return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Bug state didn't change", null))
            val ytId = body.getYtId()
            val actualIssueState = issueService.search(ytId, listOf("idReadable", "customFields(name,value(name))")).firstOrNull()
                    ?: return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Issue with id $ytId not found in YouTrack", null))
            if ((actualIssueState.customFields?.firstOrNull { it.name == "State" }?.value as? LinkedTreeMap<*, *>)?.get("name").toString() != "Направлена разработчику")
                return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Issue with id $ytId is not on 3rd line", null))
            val linkedBugs = if (bugs.isEmpty()) actualIssueState.customFields?.firstOrNull { it.name == "Issue" }?.value.toString().split(",", " ").mapNotNull { it.toIntOrNull() } else bugs as ArrayList
            val workItemId = body.resource?.workItemId ?: 0
            val currentBugState = DevOpsBugState(
                    systemId = workItemId.toString(),
                    state = body.newFieldValue("System.State").toString(),
                    sprint = body.getFieldValue("System.IterationPath").toString(),
                    sprintDates = dictionaries.sprints[body.getFieldValue("System.IterationPath").toString()],
                    stateOrder = dictionaries.devOpsStates.firstOrNull { k -> k.state == body.newFieldValue("System.State").toString() }?.order
                            ?: -1
            )
            val bugStates = if (linkedBugs.any { it != workItemId }) getComposedBugsState(linkedBugs.filter { it != workItemId }).plus(currentBugState) else listOf(currentBugState)
            val inferredState = when {
                bugStates.any { it.sprint == "\\AP\\Backlog" && it.state == "Proposed" } -> "Backlog"
                bugStates.all { it.state == "Closed" } -> "Closed"
                bugStates.all { it.state == "Closed" || it.state == "Resolved" } -> "Resolved"
                else -> bugStates.filter { it.state != "Closed" }.minBy { it.stateOrder }?.state ?: "Closed"
            }
            var fieldState: String? = null
            var fieldDetailedState: String? = null
            if (body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved")) {
                fieldState = postCommand(ytId, "Состояние Открыта", "Состояние: {Направлена разработчику}").body.toString()
            }
            if (inferredState !in arrayOf("Closed", "Resolved") && (body.isFieldChanged("System.State") || body.isFieldChanged("IterationPath"))) {
                fieldDetailedState = postCommand(ytId, "Детализированное состояние $inferredState", "Состояние: -{Ожидает подтверждения} ").body.toString()
            }
            val details = JSONObject(mapOf("ytId" to ytId,
                    "inferredState" to inferredState,
                    "actualIssueState" to actualIssueState,
                    "linkedBugs" to linkedBugs,
                    "bugStates" to bugStates,
                    "fieldState" to fieldState,
                    "fieldDetailedState" to fieldDetailedState))
            val result = saveHookToDatabase(body, fieldState, fieldDetailedState, null, Gson().toJson(details))
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        } catch (e: Error) {
            ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, e.localizedMessage, null))
        }
    }


    override fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, details: String?): Timestamp {
        return dsl
                .insertInto(Hooks.HOOKS)
                .set(Hooks.HOOKS.RECORD_DATE_TIME, Timestamp.from(Instant.now()))
                .set(Hooks.HOOKS.HOOK_BODY, Gson().toJson(body).toString())
                .set(Hooks.HOOKS.FIELD_STATE, fieldState)
                .set(Hooks.HOOKS.FIELD_DETAILED_STATE, fieldDetailedState)
                .set(Hooks.HOOKS.ERROR_MESSAGE, errorMessage)
                .set(Hooks.HOOKS.DETAILS, details)
                .returning(Hooks.HOOKS.RECORD_DATE_TIME)
                .fetchOne().recordDateTime
    }

    override fun getComposedBugsState(ids: List<Int>): List<DevOpsBugState> {
        val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${ids.joinToString(",")}) and TeamProjectCollectionSK = 37"""
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsState(it) }.map { d ->
            d.sprintDates = dictionaries.sprints[d.sprint]
            d.stateOrder = dictionaries.devOpsStates.firstOrNull { k -> k.state == d.state }?.order
                    ?: -1
            d
        }
    }
}