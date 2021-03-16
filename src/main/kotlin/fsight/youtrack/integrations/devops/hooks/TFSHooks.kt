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
    override fun handleWiUpdated(body: WiUpdatedHook?): Any {
        try {
            if (body?.subscriptionId == TEST_HOOK_ID) return pg.saveHookToDatabase(body = body, errorMessage = "This is a test hook", type = HookTypes.CHANGE)
            if (body?.eventType == "workitem.commented") return pg.saveHookToDatabase(body = body, errorMessage = "Comment was added", type = HookTypes.CHANGE)
            /**
             * Выходим если не менялись состояние и не было включения/исключения из спринта
             * */
            if (body?.isFieldChanged("System.State") != true && body?.wasIncludedToSprint() != true && body?.wasExcludedFromSprint() != true && body?.sprintHasChanged() != true && body?.isFieldChanged(
                    "System.BoardColumn"
                ) != true
            ) {
                return pg.saveHookToDatabase(body = body, errorMessage = "Bug state and sprint didn't change", type = HookTypes.CHANGE)
            }
            /**
             * Получаем номер WI из хука
             * */
            val hookWIId = body.getDevOpsId() ?: return pg.saveHookToDatabase(body = body, errorMessage = "Unable to parse WI ID", type = HookTypes.CHANGE)

            /**
             * Получаем список issue, в которые указан id WI в полях "Issue" и "Requirement"
             * */
            val issues = pg.getIssueIdsByWIId(hookWIId)
            /**
             * Выходим, если не найдены связанные issue
             * */
            if (issues.isEmpty()) return pg.saveHookToDatabase(body = body, errorMessage = "No issues are associated with WI", type = HookTypes.CHANGE)
            /**
             * Получаем информацию из YT по номерам issue
             * */
            val filter = "(${issues.joinToString(separator = " ") { "#$it" }}) и Состояние: {Направлена разработчику}"
            val actualIssues = issueService.search(filter, listOf("idReadable", "customFields(name,value(name))"))

            /**
             * На основании всех issue получаем все привязанные к ним баги
             * */
            val linkedWIIds = actualIssues.getBugsAndFeatures()

            /**
             * Выходим, если нет багов и фич
             * */
            if (linkedWIIds.isEmpty()) return pg.saveHookToDatabase(body = body, errorMessage = "No bugs found. Issues size = ${actualIssues.size}", type = HookTypes.CHANGE)
            /**
             * Получаем состояния багов из DevOps и присваиваем порядок каждому состоянию
             * */
            val devOpsItems = devops.getDevOpsItemsByIds(linkedWIIds).mergeWithHookData(body, dictionaries.devOpsStates)
            var errorMessage: String? = null
            /**
             * Для каждого issue получаем выведенное состояние и отправляем команды в YT на его основании
             * */

            actualIssues.forEach { ai ->
                /*
                * Получаем только актуальные для конкретного issue баги
                * */
                val wi = devOpsItems.filter { w -> ai.bugsAndFeatures().indexOf(w.systemId) != -1 }

                /**
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
                val area = devOpsItems.firstOrNull { it.state !in listOf("Closed", "Resolved") }?.area
                val team = if (area != null) resolver.resolveAreaToTeam(area) else null
                when {
                    area == null -> {
                    }
                    team == null -> mailSender.sendMail(
                        DEFAULT_MAIL_SENDER,
                        TEST_MAIL_RECEIVER,
                        "Area was not resolved to team",
                        "Issues ID = ${ai.idReadable}, Area = $area, DevOpsItems = $devOpsItems"
                    )
                    team != ai.unwrapEnumValue("Команда") -> {
                        cases.add(Pair(ai.idReadable ?: "", 12))
                        commands.add("Команда $team")
                    }
                }
                /**
                 * Отправляем команду в YT на основании выведенного состояния и прочих значений
                 * */
                when {
                    /**
                     * Issue закрыт либо ожидает закрытия
                     * */
                    body.movedToSupportArea() -> {
                        cases.add(Pair(ai.idReadable ?: "", 13))
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние Прокомментирована 3ЛП"))
                    }
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
                    /**
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
                    /**
                     * Если в хуке менялось состояние и выведенное состояние входит в ["Closed", "Proposed", "Resolved"] и причине изменение входит в ["Rejected", "As Designed", "Cannot Reproduce"],
                     * то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                     * Пример: FSC-972, изменение от 14 июл. 2020 10:38
                     * */
                    body.isFieldChanged("System.State")
                            && inferredState in arrayOf("Closed", "Resolved")
                            && (body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") in listOf("Rejected", "As Designed", "Cannot Reproduce") ||
                            body.getFieldValue("System.Reason") in listOf("Rejected", "As Designed", "Cannot Reproduce"))
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 8))
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние $inferredState"))
                    }
                    body.isFieldChanged("System.State") && inferredState == "Proposed"
                            && (body.getFieldValue("Microsoft.VSTS.Common.ResolvedReason") in listOf("Rejected", "As Designed", "Cannot Reproduce") ||
                            body.getFieldValue("System.Reason") in listOf("Rejected", "As Designed", "Cannot Reproduce"))
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 9))
                        commands.addAll(listOf("Состояние Открыта", "Детализированное состояние $inferredState"))
                    }
                    /**
                     * Если в хуке менялось состояние и выведенное состояние входит в ["Closed", "Proposed", "Resolved"], то issue должен вернуться в состояние "Открыта", а в детализированное состояние должно записаться выведенное состояние
                     * */
                    body.isFieldChanged("System.State") && inferredState in arrayOf("Closed", "Proposed", "Resolved") -> {
                        cases.add(Pair(ai.idReadable ?: "", 10))
                        commands.add("Детализированное состояние $inferredState")
                    }
                    /**
                     * Если выведенное состояние != закрытому, задача не исключалась из спринта и было изменено сосстояние либо итерация, состояние не должно меняться, а в детализированное состояние должно записаться выведенное состояние
                     * */
                    inferredState !in arrayOf("Closed", "Resolved") && !body.wasExcludedFromSprint()
                            && ((body.isFieldChanged("System.State") || body.isFieldChanged("System.IterationPath")))
                    -> {
                        cases.add(Pair(ai.idReadable ?: "", 11))
                        commands.add("Детализированное состояние $inferredState")
                    }
                    cases.isEmpty() -> {
                        mailSender.sendHtmlMessage(arrayOf(TEST_MAIL_RECEIVER), null, "Не найдено правило для обработки задачи ${ai.idReadable}", Gson().toJson(body))
                    }
                }
                /**
                 * Сохраняем информацию об изменениях на основе хука для последующего анализа
                 * */
                postCommand(ai.idReadable, commands.distinct().joinToString(" ")).body.toString()
                pg.saveHookToDatabase(body, null, null, errorMessage, inferredState, commands, HookTypes.CHANGE, cases)
            }
            return issues
        } catch (e: Error) {
            mailSender.sendHtmlMessage(arrayOf(TEST_MAIL_RECEIVER), null, "Ошибка при обработке хука", e.localizedMessage)
            return pg.saveHookToDatabase(body = body, errorMessage = e.localizedMessage, type = HookTypes.CHANGE)
        }
    }

    override fun handleWiCommented(body: WiCommentedHook?): Any {
        return try {
            val users = body?.getMentionedUsers()
            val supportEmails = pg.getSupportEmployees().filter { e -> users?.any { u -> (e.fullName.contains(u)) && e.isSupport } ?: false }
            if (supportEmails.isEmpty()) return pg.saveHookToDatabase(body = body, errorMessage = "No support employees", type = HookTypes.COMMENT)
            val hookWIId = body?.getDevOpsId() ?: return pg.saveHookToDatabase(body = body, errorMessage = "Unable to parse WI ID", type = HookTypes.CHANGE)
            val issues = pg.getIssueIdsByWIId(hookWIId)
            if (issues.isEmpty()) return pg.saveHookToDatabase(body = body, errorMessage = "No issues are associated with WI", type = HookTypes.COMMENT)
            val filter = "(${issues.joinToString(separator = " ") { "#$it" }}) и Состояние: {Направлена разработчику}"
            val actualIssues = issueService.search(filter, listOf("idReadable", "customFields(name,value(name))"))
            actualIssues.forEach { ai ->
                postCommand(ai.idReadable, "Детализированное состояние Прокомментирована 3ЛП").body.toString()
                pg.saveHookToDatabase(body, null, null, null, null, null, HookTypes.COMMENT, arrayListOf())
            }
            issues
        } catch (e: Error) {
            mailSender.sendHtmlMessage(arrayOf(TEST_MAIL_RECEIVER), null, "Ошибка при обработке хука", e.localizedMessage)
            pg.saveHookToDatabase(body = body, errorMessage = e.localizedMessage, type = HookTypes.COMMENT)
        }
    }
}
