package fsight.youtrack.integrations.devops.hooks

import com.google.gson.Gson
import fsight.youtrack.*
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.db.IDevOpsProvider
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Hooks.HOOKS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldValuesRecord
import fsight.youtrack.mail.IMailSender
import fsight.youtrack.models.DevOpsWorkItem
import fsight.youtrack.models.hooks.Hook
import fsight.youtrack.models.youtrack.Command
import fsight.youtrack.models.youtrack.Issue
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class TFSHooks(
    private val dsl: DSLContext
) : ITFSHooks {
    @Autowired
    lateinit var issueService: IIssue

    @Autowired
    lateinit var dictionaries: IDictionary

    @Autowired
    lateinit var mailSender: IMailSender

    @Autowired
    lateinit var ms: IDevOpsProvider

    @Autowired
    lateinit var pg: IPGProvider

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
            .select(HOOKS.HOOK_BODY)
            .from(HOOKS)
            .orderBy(HOOKS.RECORD_DATE_TIME.desc())
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
            if (body?.subscriptionId == "00000000-0000-0000-0000-000000000000") {
                pg.saveHookToDatabase(body, null, null, "This is a test hook", "null")
                ResponseEntity.status(HttpStatus.CREATED).body("This is a test hook")
            }
            /**
             * Выходим если не менялись состояние и не было включения/исключения из спринта
             * */
            if (body?.isFieldChanged("System.State") != true && body?.wasIncludedToSprint() != true && body?.wasExcludedFromSprint() != true && body?.sprintHasChanged() != true && body?.isFieldChanged(
                    "System.BoardColumn"
                ) != true
            ) {
                return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "Bug state and sprint didn't change", null))
            }
            /**
             * Получаем номер WI из хука
             * */
            val hookWIId = body.getDevOpsId() ?: return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "Unable to parse WI ID", null))
            /*
            * Получаем список issue, в которые указан id WI в полях "Issue" и "Requirement"
            * */
            val issues = getIssuesByWIId(hookWIId)
            /**
             * Выходим, если не найдены связанные issue
             * */
            if (issues.isEmpty()) return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "No issues are associated with WI", null))
            /**
             * Получаем информацию из YT по номерам issue
             * */
            val filter = "(${issues.joinToString(separator = " ") { "#$it" }}) и Состояние: {Направлена разработчику}"
            val actualIssues =
                issueService.search(filter, listOf("idReadable", "customFields(name,value(name))"))
            /*
            * На основании всех issue получаем все привязанные к ним баги
            * */
            val linkedWIIds = actualIssues.getBugsAndFeatures()

            /*
            * Выходим, если нет багов и фич
            * */
            if (linkedWIIds.isEmpty()) return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "No bugs found. Issues size = ${actualIssues.size}", null))
            /*
            * Получаем состояния багов из DevOps и присваиваем порядок каждому состоянию
            * */
            val devOpsStates = ms.getDevOpsBugsState(linkedWIIds).mergeWithHookData(body, dictionaries.devOpsStates)

            var fieldState: String? = null
            var fieldDetailedState: String? = null
            var errorMessage: String? = null

            /*
            * Для каждого issue получаем выведенное состояние и отправляем команды в YT на его основании
            * */
            val cases = arrayListOf<Pair<String, Int>>()
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
                //TODO выведение состояния должно учитывать поле Reason
                val inferredState = getInferredState(wi)
                val issueState = ai.unwrapFieldValue("State")
                val issueDetailedState = ai.unwrapFieldValue("Детализированное состояние")
                val sprint = wi.getLastSprint()
                when {
                    body.sprintHasChanged() /*&& ai.idReadable?.contains("SA-") == true*/ && (sprint == "Backlog" || sprint == null) -> {
                        cases.add(Pair(ai.idReadable ?: "", 1))
                        val s = postCommand(ai.idReadable, "Sprints $sprint Детализированное состояние Backlog").body.toString()
                        pg.saveHookToDatabase(body, s, "Backlog", null, null)
                    }
                    body.sprintHasChanged() /*&& ai.idReadable?.contains("SA-") == true*/ && sprint != null -> {
                        cases.add(Pair(ai.idReadable ?: "", 2))
                        val s = postCommand(ai.idReadable, "Sprints $sprint").body.toString()
                        pg.saveHookToDatabase(body, s, null, null, null)
                    }
                }
                /**
                 * Отправляем команду в YT на основании выведенного состояния и прочих значений
                 * */
                when {
                    /**
                     * Issue закрыт либо ожидает закрытия
                     * */
                    body.isFeature()
                            && body.isFieldChanged("System.BoardColumn")
                            && body.newFieldValue("System.BoardColumn") in listOf("На уточнении", "Отклонено") -> {
                        cases.add(Pair(ai.idReadable ?: "", 3))
                        fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние Backlog 2ЛП").body.toString()
                    }
                    issueState in listOf("Ожидает ответа", "Ожидает подтверждения", "Incomplete", "Подтверждена", "Без подтверждения") -> {
                        cases.add(Pair(ai.idReadable ?: "", 4))
                        errorMessage = "Нельзя применить изменения к issue в состоянии $issueState"
                    }
                    issueDetailedState in listOf("Ожидает сборку") -> {
                        cases.add(Pair(ai.idReadable ?: "", 5))
                        errorMessage = "Нельзя применить изменения к issue в детализированном состоянии $issueDetailedState"
                    }
                    /*
                    * Если выведенное состояние входит в ["Closed", "Resolved"], было изменено сосстояние и причина закрытия заявки входит в ["Fixed", "Verified"], то issue должен вернуться в состояние "Открыта", а детализированное состояние должно перейти в "Backlog проверки"
                    * */
                    inferredState in arrayOf("Closed", "Resolved")
                            && body.isFieldChanged("System.State")
                            && body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") in listOf("Fixed", "Verified")

                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 6))
                        fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние Backlog проверки").body.toString()
                    }
                    /**
                     * Если выведенное состояние входит в ["Closed", "Resolved"], было изменено сосстояние и причина закрытия заявки = Rejected, то issue должен вернуться в состояние "Открыта", а детализированное состояние должно перейти в "Backlog 2ЛП"
                     * */
                    inferredState in arrayOf("Closed", "Resolved") && body.isFieldChanged("System.State")
                            && body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") == "Rejected"
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 7))
                        fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние Backlog 2ЛП").body.toString()
                    }
                    /*
                    * Если в хуке менялось состояние и выведенное состояние входит в ["Closed", "Proposed", "Resolved"] и причине изменение входит в ["Rejected", "As Designed", "Cannot Reproduce"],
                    * то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                    * Пример: FSC-972, изменение от 14 июл. 2020 10:38
                    * */
                    body.isFieldChanged("System.State")
                            && inferredState in arrayOf("Closed", "Proposed", "Resolved")
                            && (body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") in listOf("Rejected", "As Designed", "Cannot Reproduce") ||
                            body.getFieldValue("System.Reason") in listOf("Rejected", "As Designed", "Cannot Reproduce"))
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 8))
                        fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние $inferredState").body.toString()
                    }
                    /*
                    * Если в хуке менялось состояние и выведенное состояние входит в ["Closed", "Proposed", "Resolved"], то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                    * */
                    body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved") -> {
                        /*fieldState = postCommand(ai.idReadable, "Состояние Открыта").body.toString()*/
                        cases.add(Pair(ai.idReadable ?: "", 9))
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние $inferredState").body.toString()
                    }
                    /*
                    * Если выведенное состояние != закрытому, задача не исключалась из спринта и было изменено сосстояние либо итерация, состояние не должно меняться, а в детализированное состояние должно записаться выведенное состояние
                    * */
                    inferredState !in arrayOf("Closed", "Resolved") && !body.wasExcludedFromSprint()
                            && ((body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath")))
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 10))
                        fieldDetailedState = postCommand(ai.idReadable, "Детализированное состояние $inferredState").body.toString()
                    }
                }
                /**
                 * Сохраняем информацию об изменениях на основе хука для последующего анализа
                 * */
                pg.saveHookToDatabase(body, fieldState, fieldDetailedState, errorMessage, inferredState)
            }
            ResponseEntity.status(HttpStatus.CREATED).body(cases)
        } catch (e: Error) {
            mailSender.sendHtmlMessage(TEST_MAIL_RECEIVER, null, "Ошибка при обработке хука", e.localizedMessage)
            ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, e.localizedMessage, null))
        }
    }

    override fun getIssuesByWIId(id: Int): List<String> {
        return dsl
            .select(CUSTOM_FIELD_VALUES.ISSUE_ID, CUSTOM_FIELD_VALUES.FIELD_VALUE)
            .from(CUSTOM_FIELD_VALUES)
            .where(CUSTOM_FIELD_VALUES.FIELD_NAME.`in`(listOf("Issue", "Requirement")).and(CUSTOM_FIELD_VALUES.FIELD_VALUE.like("%%$id%%")))
            .fetchInto(CustomFieldValuesRecord::class.java)
            .filter { it.fieldValue.replace(" ", "").splitToList().contains(id.toString()) }
            .map { it.issueId }
    }

    override fun getInferredState(bugStates: List<DevOpsWorkItem>): String {
        return when {
            bugStates.any { it.sprint == "\\AP\\Backlog" && it.state == "Proposed" } -> "Backlog"
            bugStates.any { it.state == "Proposed" } -> "Proposed"
            bugStates.all { it.state == "Closed" } -> "Closed"
            bugStates.all { it.state == "Closed" || it.state == "Resolved" } -> "Resolved"
            else -> bugStates.filter { it.state != "Closed" }.minBy { it.stateOrder }?.state ?: "Closed"
        }
    }

    override fun getInferredSprint(bugStates: List<DevOpsWorkItem>): String {
        return ""
    }
}
