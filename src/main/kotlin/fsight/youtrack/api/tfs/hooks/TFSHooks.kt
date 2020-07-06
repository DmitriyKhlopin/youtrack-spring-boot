package fsight.youtrack.api.tfs.hooks

import com.google.gson.Gson
import com.google.gson.JsonObject
import fsight.youtrack.*
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Hooks
import fsight.youtrack.generated.jooq.tables.Hooks.HOOKS
import fsight.youtrack.models.DevOpsBugState
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.models.youtrack.Command
import fsight.youtrack.models.youtrack.Issue
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant


@Service
class TFSHooks(
    private val dsl: DSLContext,
    @Qualifier("tfsDataSource") private val ms: Database
) : ITFSHooks {
    @Autowired
    lateinit var issueService: IIssue

    @Autowired
    lateinit var dictionaries: IDictionary

    /**
     * Получение [limit] последних хуков из лога.
     * */
    override fun getHook(limit: Int): ResponseEntity<Any> {
        val i = dsl
            .select(HOOKS.HOOK_BODY)
            .from(HOOKS)
            .orderBy(HOOKS.RECORD_DATE_TIME.desc())
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

    /**
     * Отправка команды в YT. Команда [command] отправляется дли issue с idReadable = [id]
     * */
    override fun postCommand(id: String?, command: String): ResponseEntity<Any> {
        val cmd = Gson().toJson(Command(issues = arrayListOf(Issue(idReadable = id)), query = command))
        val response = YouTrackAPI.create().postCommand(DEVOPS_AUTH, cmd).execute()
        return ResponseEntity.ok("Issue $id returned response code ${response.code()} on command: $command")
    }

    /**
     * Обработка хука из DevOps.
     * */
    override fun postHook(body: Hook?): ResponseEntity<Any> {
        return try {
            /**
             * Выходим если не менялись состояние и не было включения/исключения из спринта
             * */
            if (body?.isFieldChanged("System.State") != true && body?.wasIncludedToSprint() != true && body?.wasExcludedFromSprint() != true) {
                return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Bug state and sprint didn't change", null))
            }
            /**
             * Получаем номер WI из хука
             * */
            val hookWIId = body.getDevOpsId() ?: return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "Unable to parse WI ID", null))
            /*
            * Получаем список issue, в которые указан id WI в полях "Issue" и "Requirement"
            * */
            val issues = getIssuesByWIId(hookWIId)
            /**
             * Выходим, если не найдены связанные issue
             * */
            if (issues.isEmpty()) return ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, "No issues are associated with WI", null))
            /**
             * Получаем информацию из YT по номерам issue
             * */
            val actualIssues = issueService.search(issues.joinToString(separator = " ") { "#$it" }, listOf("idReadable", "customFields(name,value(name))"))
            /*
            * На основании всех issue получаем все привязанные к ним баги
            * */
            val linkedWIIds = actualIssues.getBugsAndFeatures()
            /*
            * Получаем состояния багов из DevOps и присваиваем порядок каждому состоянию
            * */
            val devOpsStates = getDevOpsBugsState(linkedWIIds).mergeWithHookData(body, dictionaries.devOpsStates)

            var fieldState: String? = null
            var fieldDetailedState: String? = null

            /*
            * Для каждого issue получаем выведенное состояние и отправляем команды в YT на его основании
            * */
            actualIssues.forEach { ai ->
                fieldState = null
                fieldDetailedState = null
                /*
                * Получаем только актуальные для конкретного issue баги
                * */
                val wi = devOpsStates.filter { w -> ai.bugsAndFeatures().indexOf(w.systemId) != -1 }
                /*
                * Получаем выведенное состояние
                * */
                val inferredState = getInferredState(wi)

                /**
                 * Отправляем команду в YT на основании выведенного состояния и прочих значений
                 * */
                when {
                    /*
                    * Если выведенное состояние входит в ["Closed", "Resolved"], было изменено сосстояние и причина закрытия заявки = Fixed, то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                    * */
                    inferredState in arrayOf("Closed", "Resolved") && body.isFieldChanged("System.State")
                            && body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") == "Fixed"
                    -> {
                        fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние Backlog проверки").body.toString()
                    }
                    /*
                    * //TODO Нужно изменить доску!!! Статус "Resolved" должен быть перенесён к "Closed"
                    * Если в хуке менялось состояние и выведенное состояние входит в ["Closed", "Proposed", "Resolved"], то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                    * */
                    body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved") -> {
                        fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние $inferredState").body.toString()
                    }
                    /*
                    * Если выведенное состояние != закрытому, задача не исключалась из спринта и было изменено сосстояние либо итерация, состояние не должно меняться, а в детализированное состояние должно записаться выведенное состояние
                    * */
                    inferredState !in arrayOf("Closed", "Resolved") && !body.wasExcludedFromSprint()
                            && ((body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath")))
                    -> {
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние $inferredState").body.toString()
                    }


                }
                /**
                 * Сохраняем информацию об изменениях на основе хука для последующего анализа
                 * */
                saveHookToDatabase(body, fieldState, fieldDetailedState, null, inferredState)
            }

            /*

            val result = JSONObject(
                mapOf(
                    "timestamp" to saveHookToDatabase(body, fieldState, fieldDetailedState, null, inferredState),
                    "ytId" to ytId,
                    "inferredState" to inferredState,
                    "actualIssueState" to actualIssueState,
                    "linkedBugs" to linkedBugs,
                    "fieldState" to fieldState,
                    "fieldDetailedState" to fieldDetailedState,
                    "inferredState" to inferredState
                )
            )*/
            ResponseEntity.status(HttpStatus.CREATED).body(null)
        } catch (e: Error) {
            ResponseEntity.status(HttpStatus.CREATED).body(saveHookToDatabase(body, null, null, e.localizedMessage, null))
        }
    }

    override fun getIssuesByWIId(id: Int): List<String> {
        return dsl
            .select(CUSTOM_FIELD_VALUES.ISSUE_ID)
            .from(CUSTOM_FIELD_VALUES)
            .where(CUSTOM_FIELD_VALUES.FIELD_NAME.`in`(listOf("Issue", "Feature")).and(CUSTOM_FIELD_VALUES.FIELD_VALUE.like("%%$id%%")))
            .fetchInto(String::class.java)
    }

    override fun saveHookToDatabase(body: Hook?, fieldState: String?, fieldDetailedState: String?, errorMessage: String?, inferredState: String?): Timestamp {
        return dsl
            .insertInto(HOOKS)
            .set(HOOKS.RECORD_DATE_TIME, Timestamp.from(Instant.now()))
            .set(HOOKS.HOOK_BODY, Gson().toJson(body).toString())
            .set(HOOKS.FIELD_STATE, fieldState)
            .set(HOOKS.FIELD_DETAILED_STATE, fieldDetailedState)
            .set(HOOKS.ERROR_MESSAGE, errorMessage)
            .set(HOOKS.INFERRED_STATE, inferredState)
            .returning(HOOKS.RECORD_DATE_TIME)
            .fetchOne().recordDateTime
    }

    override fun getDevOpsBugsState(ids: List<Int>): List<DevOpsBugState> {
        val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${ids.joinToString(",")}) and TeamProjectCollectionSK = 37"""
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsState(it) }
    }

    override fun getInferredState(bugStates: List<DevOpsBugState>): String {
        return when {
            bugStates.any { it.sprint == "\\AP\\Backlog" && it.state == "Proposed" } -> "Backlog"
            bugStates.any { it.state == "Proposed" } -> "Proposed"
            //TODO FSC-951
            bugStates.all { it.state == "Closed" } -> "Closed"
            bugStates.all { it.state == "Closed" || it.state == "Resolved" } -> "Resolved"
            else -> bugStates.filter { it.state != "Closed" }.minBy { it.stateOrder }?.state ?: "Closed"
        }
    }

    override fun getAssociatedBugsState(id: String): JsonObject? {
        val issues = dsl
            .select(CUSTOM_FIELD_VALUES.FIELD_VALUE)
            .from(CUSTOM_FIELD_VALUES)
            .where(CUSTOM_FIELD_VALUES.ISSUE_ID.eq(id))
            .and(CUSTOM_FIELD_VALUES.FIELD_NAME.eq("Issue"))
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