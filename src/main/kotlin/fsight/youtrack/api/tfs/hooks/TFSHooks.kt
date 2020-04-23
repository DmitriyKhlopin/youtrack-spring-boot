package fsight.youtrack.api.tfs.hooks

import com.google.gson.Gson
import com.google.gson.JsonObject
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
            when {
                body?.isFieldChanged("System.State") == true && body.wasIncludedToSprint() -> {
                }
                body?.isFieldChanged("System.State") == true -> {
                }
                body?.wasIncludedToSprint() == true -> {
                }
                else -> return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Bug state and sprint didn't change"))
            }
            /*if (body?.isFieldChanged("System.State") != true && body?.wasIncludedToSprint() != true) return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Bug state didn't change"))*/
            val ytId = body.getYtId()
            val actualIssueState = issueService.search("#$ytId", listOf("idReadable", "customFields(name,value(name))")).firstOrNull()
                    ?: return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Issue with id $ytId not found in YouTrack"))
            val actualIssueFieldState = actualIssueState.unwrapFieldValue("State")
            if (actualIssueFieldState != "Направлена разработчику") return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, actualIssueFieldState, null, "Issue with id $ytId is not on 3rd line"))
            val linkedBugs = if (bugs.isEmpty()) actualIssueState.customFields?.firstOrNull { it.name == "Issue" }?.value.toString().split(",", " ").mapNotNull { it.toIntOrNull() } else bugs
            val bugStates = getDevOpsBugsState(linkedBugs).map {
                if (it.systemId == body.resource?.workItemId.toString()) {
                    it.state = body.getFieldValue("System.State").toString()
                    it.sprint = body.getFieldValue("System.IterationPath").toString()
                }
                it
            }.map {
                it.sprintDates = dictionaries.sprints[it.sprint]
                it.stateOrder = dictionaries.devOpsStates.firstOrNull { k -> k.state == it.state }?.order ?: -1
                it
            }

            val inferredState = when {
                bugStates.any { it.sprint == "\\AP\\Backlog" && it.state == "Proposed" } -> "Backlog"
                bugStates.any { it.state == "Proposed" } -> "Proposed"
                bugStates.all { it.state == "Closed" } -> "Closed"
                bugStates.all { it.state == "Closed" || it.state == "Resolved" } -> "Resolved"
                else -> bugStates.filter { it.state != "Closed" }.minBy { it.stateOrder }?.state ?: "Closed"
            }
            var fieldState: String? = null
            var fieldDetailedState: String? = null
            if (body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved")) {
                fieldState = postCommand(ytId, "Состояние Открыта", "Состояние: {Направлена разработчику}").body.toString()
            }
            if (inferredState !in arrayOf("Closed", "Resolved") && !body.wasExcludedFromSprint() && (body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath"))) {
                fieldDetailedState = postCommand(ytId, "Детализированное состояние $inferredState", "Состояние: {Направлена разработчику}").body.toString()
            }
            val result = JSONObject(mapOf("timestamp" to saveHookToDatabase(body, fieldState, fieldDetailedState, null),
                    "ytId" to ytId,
                    "inferredState" to inferredState,
                    "actualIssueState" to actualIssueState,
                    "linkedBugs" to linkedBugs,
                    "fieldState" to fieldState,
                    "fieldDetailedState" to fieldDetailedState))
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        } catch (e: Error) {
            ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, e.localizedMessage))
        }
    }


    override fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?): Timestamp {
        return dsl
                .insertInto(Hooks.HOOKS)
                .set(Hooks.HOOKS.RECORD_DATE_TIME, Timestamp.from(Instant.now()))
                .set(Hooks.HOOKS.HOOK_BODY, Gson().toJson(body).toString())
                .set(Hooks.HOOKS.FIELD_STATE, fieldState)
                .set(Hooks.HOOKS.FIELD_DETAILED_STATE, fieldDetailedState)
                .set(Hooks.HOOKS.ERROR_MESSAGE, errorMessage)
                .returning(Hooks.HOOKS.RECORD_DATE_TIME)
                .fetchOne().recordDateTime
    }

    override fun getDevOpsBugsState(ids: List<Int>): List<DevOpsBugState> {
        val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${ids.joinToString(",")}) and TeamProjectCollectionSK = 37"""
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsState(it) }
    }

    override fun mergeStates(devOpsBugStates: List<DevOpsBugState>, hook: Hook) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAssociatedBugsState(id: String): JsonObject? {
        val issues = dsl.select(CustomFieldValues.CUSTOM_FIELD_VALUES.FIELD_VALUE)
                .from(CustomFieldValues.CUSTOM_FIELD_VALUES)
                .where(CustomFieldValues.CUSTOM_FIELD_VALUES.ISSUE_ID.eq(id))
                .and(CustomFieldValues.CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Issue"))
                .fetchOneInto(String::class.java)

        val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${issues}) and TeamProjectCollectionSK = 37"""
        val all = statement.execAndMap(ms) { ExposedTransformations().toJsonObject(it, listOf("System_Id", "System_State", "IterationPath")) }
                .map { e ->
                    e.addProperty("order", dictionaries.devOpsStates.firstOrNull { k -> k.state == e["System_State"].asString }?.order)
                    e
                }
        val filtered = all.filter { it["IterationPath"].asString != "Backlog" && it["System_State"].asString != "Closed" && it["System_State"].asString != "Resolved" }
        return if (filtered.isEmpty()) all.minBy { it["order"].toString().toInt() } else filtered.minBy { it["order"].toString().toInt() }
    }
}