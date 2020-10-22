package fsight.youtrack.integrations.devops.hooks

import com.google.gson.Gson
import fsight.youtrack.*
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.common.IResolver
import fsight.youtrack.db.IDevOpsProvider
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.mail.IMailSender
import fsight.youtrack.models.hooks.*
import fsight.youtrack.models.youtrack.Command
import fsight.youtrack.models.youtrack.Issue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class TFSHooks : ITFSHooks {
    @Autowired
    lateinit var issueService: IIssue

    @Autowired
    lateinit var dictionaries: IDictionary

    @Autowired
    lateinit var mailSender: IMailSender

    @Autowired
    lateinit var devops: IDevOpsProvider

    @Autowired
    lateinit var pg: IPGProvider

    @Autowired
    lateinit var resolver: IResolver

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
    override fun handleWiUpdated(body: WiUpdatedHook?): ResponseEntity<Any> {
        return try {
            if (body?.subscriptionId == "00000000-0000-0000-0000-000000000000") {
                pg.saveHookToDatabase(body, null, null, "This is a test hook", "null", null, HookTypes.CHANGE.name, null)
                ResponseEntity.status(HttpStatus.CREATED).body("This is a test hook")
            }
            if (body?.eventType == "workitem.commented") {
                return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "Comment was added", null, null, HookTypes.CHANGE.name, null))
            }
            /**
             * Выходим если не менялись состояние и не было включения/исключения из спринта
             * */
            if (body?.isFieldChanged("System.State") != true && body?.wasIncludedToSprint() != true && body?.wasExcludedFromSprint() != true && body?.sprintHasChanged() != true && body?.isFieldChanged(
                    "System.BoardColumn"
                ) != true
            ) {
                return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "Bug state and sprint didn't change", null, null, HookTypes.CHANGE.name, null))
            }
            /**
             * Получаем номер WI из хука
             * */
            val hookWIId =
                body.getDevOpsId() ?: return ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, "Unable to parse WI ID", null, null, HookTypes.CHANGE.name, null))
            /*
            * Получаем список issue, в которые указан id WI в полях "Issue" и "Requirement"
            * */
            val issues = pg.getIssueIdsByWIId(hookWIId)
            /**
             * Выходим, если не найдены связанные issue
             * */
            if (issues.isEmpty()) return ResponseEntity.status(HttpStatus.CREATED)
                .body(pg.saveHookToDatabase(body, null, null, "No issues are associated with WI", null, null, HookTypes.CHANGE.name, null))
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
            if (linkedWIIds.isEmpty()) return ResponseEntity.status(HttpStatus.CREATED)
                .body(pg.saveHookToDatabase(body, null, null, "No bugs found. Issues size = ${actualIssues.size}", null, null, HookTypes.CHANGE.name, null))
            /*
            * Получаем состояния багов из DevOps и присваиваем порядок каждому состоянию
            * */
            val devOpsItems = devops.getDevOpsItemsByIds(linkedWIIds).mergeWithHookData(body, dictionaries.devOpsStates)
            var errorMessage: String? = null
            /*
            * Для каждого issue получаем выведенное состояние и отправляем команды в YT на его основании
            * */

            actualIssues.forEach { ai ->
                /*
                * Получаем только актуальные для конкретного issue баги
                * */
                val wi = devOpsItems.filter { w -> ai.bugsAndFeatures().indexOf(w.systemId) != -1 }
                /*
                * Получаем выведенное состояние
                * */
                //TODO выведение состояния должно учитывать поле Reason
                val inferredState = wi.getInferredState()
                val issueState = ai.unwrapFieldValue("State")
                val issueDetailedState = ai.unwrapFieldValue("Детализированное состояние")
                val sprint = wi.getLastSprint()
                val cases: ArrayList<Pair<String, Int>> = arrayListOf()
                val commands: ArrayList<String> = arrayListOf()
                when {
                    body.sprintHasChanged() && (sprint == "Backlog" || sprint == null) -> {
                        cases.add(Pair(ai.idReadable ?: "", 1))
                        commands.add("Sprints $sprint Детализированное состояние Backlog")
                    }
                    body.sprintHasChanged() && sprint != null -> {
                        cases.add(Pair(ai.idReadable ?: "", 2))
                        commands.add("Sprints $sprint")
                    }
                }

                val team = devOpsItems.first { it.state !in listOf("Closed", "Resolved") }.area?.let { area -> resolver.resolveAreaToTeam(area) }
                if (team != null && team != ai.unwrapEnumValue("Команда")) {
                    commands.add("Команда $team")
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
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние Backlog 2ЛП"))
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
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние Backlog проверки"))
                    }
                    /**
                     * Если выведенное состояние входит в ["Closed", "Resolved"], было изменено сосстояние и причина закрытия заявки = Rejected, то issue должен вернуться в состояние "Открыта", а детализированное состояние должно перейти в "Backlog 2ЛП"
                     * */
                    inferredState in arrayOf("Closed", "Resolved") && body.isFieldChanged("System.State")
                            && body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") == "Rejected"
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 7))
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние Backlog 2ЛП"))
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
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние $inferredState"))
                    }
                    /*
                    * Если в хуке менялось состояние и выведенное состояние входит в ["Closed", "Proposed", "Resolved"], то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                    * */
                    body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved") -> {
                        cases.add(Pair(ai.idReadable ?: "", 9))
                        commands.add("Детализированное состояние $inferredState")
                    }
                    /*
                    * Если выведенное состояние != закрытому, задача не исключалась из спринта и было изменено сосстояние либо итерация, состояние не должно меняться, а в детализированное состояние должно записаться выведенное состояние
                    * */
                    inferredState !in arrayOf("Closed", "Resolved") && !body.wasExcludedFromSprint()
                            && ((body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath")))
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 10))
                        commands.add("Детализированное состояние $inferredState")
                    }
                }
                /**
                 * Сохраняем информацию об изменениях на основе хука для последующего анализа
                 * */
                postCommand(ai.idReadable, commands.distinct().joinToString(" ")).body.toString()
                pg.saveHookToDatabase(body, null, null, errorMessage, inferredState, commands, HookTypes.CHANGE.name, cases)
            }
            ResponseEntity.status(HttpStatus.CREATED).body(null)
        } catch (e: Error) {
            mailSender.sendHtmlMessage(TEST_MAIL_RECEIVER, null, "Ошибка при обработке хука", e.localizedMessage)
            ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, e.localizedMessage, null, null, HookTypes.CHANGE.name, null))
        }
    }

    override fun handleWiCommented(body: WiCommentedHook?): ResponseEntity<Any> {
        return  try {
            val emails = pg.getSupportEmployees().filter { e -> body?.getMentionedUsers()?.any { u -> e.email.contains(u) } ?: false }.map { it.email }
            pg.saveHookToDatabase(
                body,
                null,
                null,
                null,
                null,
                arrayListOf("Should notify ${body?.getMentionedUsers()?.joinToString()} - ${if (emails.isEmpty()) "no support employees" else emails.joinToString()}"),
                HookTypes.COMMENT.name,
                null
            )
            ResponseEntity.status(HttpStatus.CREATED).body(null)
        } catch (e: Error) {
            mailSender.sendHtmlMessage(TEST_MAIL_RECEIVER, null, "Ошибка при обработке хука", e.localizedMessage)
            ResponseEntity.status(HttpStatus.CREATED).body(pg.saveHookToDatabase(body, null, null, e.localizedMessage, null, null, HookTypes.COMMENT.name, null))
        }
    }
}
