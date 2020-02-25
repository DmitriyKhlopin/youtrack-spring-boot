package fsight.youtrack.api.tfs

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import fsight.youtrack.AUTH
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.api.common.ICommon
import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.etl.projects.IProjects
import fsight.youtrack.generated.jooq.tables.TfsLinks.TFS_LINKS
import fsight.youtrack.generated.jooq.tables.TfsTasks.TFS_TASKS
import fsight.youtrack.generated.jooq.tables.TfsWi.TFS_WI
import fsight.youtrack.headers
import fsight.youtrack.models.*
import fsight.youtrack.models.youtrack.Issue
import fsight.youtrack.models.youtrack.Command
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooq.DSLContext
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class TFSData(
        private val dslContext: DSLContext,
        @Qualifier("tfsDataSource") private val ms: Database,
        private val projectsService: IProjects,
        private val commonService: ICommon,
        private val issueService: IIssue,
        private val dictionaries: IDictionary
) : ITFSData {
    private final val types: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also {
            /*it["jetbrains.charisma.customfields.complex.version.VersionBundle"] =
                    "jetbrains.charisma.customfields.complex.version.MultiVersionIssueCustomField"*/
            it["jetbrains.charisma.customfields.complex.version.VersionBundle"] =
                    "jetbrains.charisma.customfields.complex.version.SingleVersionIssueCustomField"
            it["jetbrains.charisma.customfields.complex.state.StateBundle"] =
                    "jetbrains.charisma.customfields.complex.state.StateIssueCustomField"
            it["jetbrains.charisma.customfields.complex.ownedField.OwnedBundle"] =
                    "jetbrains.charisma.customfields.complex.ownedField.SingleOwnedIssueCustomField"
            it["jetbrains.charisma.customfields.complex.enumeration.EnumBundle"] =
                    "jetbrains.charisma.customfields.complex.enumeration.SingleEnumIssueCustomField"
            /*it["jetbrains.charisma.customfields.complex.version.VersionBundle"] =
                    "jetbrains.charisma.customfields.complex.version.SingleVersionIssueCustomField"*/
            it["jetbrains.charisma.customfields.complex.user.UserBundle"] =
                    "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField"
            /*it["jetbrains.charisma.customfields.complex.user.UserBundle"] =
                    "jetbrains.charisma.customfields.simple.common.SimpleIssueCustomField"*/
        }
    }

    override fun getItemsCount(): Int {
        return dslContext
                .selectCount()
                .from(TFS_WI)
                .fetchOneInto(Int::class.java)
    }

    override fun getItems(offset: Int?, limit: Int?): ResponseEntity<Any> {
        val items = dslContext
                .select()
                .from(TFS_WI)
                .orderBy(TFS_WI.ID)
                .limit(limit ?: getItemsCount())
                .offset(offset ?: 0)
                .fetchInto(TFSRequirement::class.java)
        return ResponseEntity.status(HttpStatus.OK).body(items)
    }

    override fun getItemById(id: Int): TFSRequirement {
        return dslContext
                .select()
                .from(TFS_WI)
                .where(TFS_WI.ID.eq(id))
                .limit(1)
                .fetchOneInto(TFSRequirement::class.java)
    }

    override fun postItemsToYouTrack(offset: Int?, limit: Int?): ResponseEntity<Any> {
        val items = dslContext.select(
                TFS_WI.ID.`as`("id"),
                TFS_WI.REV.`as`("rev"),
                TFS_WI.CREATE_DATE.`as`("createDate")
        )
                .from(TFS_WI)
                .orderBy(TFS_WI.ID)
                .limit(limit ?: getItemsCount())
                .offset(offset ?: 0)
                .fetchInto(TFSRequirement::class.java).map { item -> item.id }

        items.forEach { postEachItem(it) }
        return ResponseEntity.status(HttpStatus.OK).body(items)
    }

    override fun postItemToYouTrack(id: Int): ResponseEntity<Any> {
        postEachItem(id)
        return ResponseEntity.status(HttpStatus.OK).body("OK")
    }

    override fun postItemsToYouTrack(iteration: String?): ResponseEntity<Any> {
        val iterations = iteration?.split(",")
        val items = dslContext.select(
                TFS_WI.ID.`as`("id"),
                TFS_WI.REV.`as`("rev"),
                TFS_WI.CREATE_DATE.`as`("createDate")
        )
                .from(TFS_WI)
                .where(TFS_WI.ITERATION_PATH.`in`(iterations))
                .orderBy(TFS_WI.ID)
                .fetchInto(TFSRequirement::class.java).map { item -> item.id }
        items.forEach {
            postEachItem(it)
        }
        return ResponseEntity.status(HttpStatus.OK).body(items)
    }

    fun postEachItem(id: Int) {
        val item = getItemById(id)
        val postableItem = getPostableRequirement(item)
        /*val id2 = YouTrackAPI.create().createIssue(AUTH, postableItem).execute()
        val idReadable = Gson().fromJson(id2.body(), YouTrackIssue::class.java)
        if (id2.errorBody() == null) getTasks(requirement = item, parentId = idReadable.idReadable ?: "")*/
    }


    @Suppress("UNUSED_VARIABLE", "unused")
    fun getTasks(requirement: TFSRequirement, parentId: String) {
        val r = dslContext.select()
                .from(TFS_TASKS)
                .leftJoin(TFS_LINKS).on(TFS_TASKS.ID.eq(TFS_LINKS.TARGET_ID))
                .where(TFS_LINKS.SOURCE_ID.eq(requirement.id))
                .fetchInto(TFSTask::class.java).map { item -> getPostableTask(item, requirement.iterationPath) }
        r.forEach {
            val id2 = YouTrackAPI.create().createIssue(AUTH, it).execute()
            if (id2.errorBody() != null) println(it)
            val issue = Gson().fromJson(id2.body(), Issue::class.java)
            val command = Gson().toJson(
                    Command(
                            issues = arrayListOf(Issue(idReadable = issue.idReadable)),
                            silent = true,
                            query = "подзадача $parentId"
                    )
            )
            //TODO implement commands
            /*YouTrackAPI.create().postCommand(AUTH, command).execute()*/
        }
    }


    override fun toJson(id: Int): ResponseEntity<Any> {
        val item = getItemById(id)
        val postableItem = getPostableRequirement(item)
        return ResponseEntity.status(HttpStatus.OK).body(postableItem)
    }

    fun getCustomFieldValue(projectName: String, fieldName: String, value: String?): FieldValueBase? {
        return when (fieldName) {
            "Assignee" -> dictionaries.users.firstOrNull { it.profile?.email?.email == value }.let { it ->
                if (it != null) SingleFieldValue(
                        id = "86-16",
                        `$type` = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField",
                        value = ActualValue(
                                id = it.id
                                        ?: "1-1", name = it.fullName ?: "admin"
                        )
                ) else null
            }
            "Issue" -> StringFieldValue(
                    id = "114-23",
                    `$type` = "jetbrains.charisma.customfields.simple.common.SimpleIssueCustomField",
                    value = value
            )
            "State" -> dictionaries.customFieldValues.asSequence().firstOrNull {
                it.fieldName == fieldName && it.name == value && it.projectName == projectName
            }.let { it ->
                if (it != null) SingleFieldValue(
                        id = it.fieldId,
                        /*`$type` = "jetbrains.charisma.customfields.complex.state.StateIssueCustomField",*/
                        `$type` = "jetbrains.charisma.workflow.statemachine.StateMachineIssueCustomField",
                        /*`$type` = "jetbrains.charisma.customfields.complex.state.StateBundleElement",*/
                        value = ActualValue(id = it.id, name = it.name)
                ) else null
            }
            "Affected versions" -> dictionaries.customFieldValues.asSequence().firstOrNull {
                it.fieldName == fieldName && it.name == value && it.projectName == projectName
            }.let { it ->
                if (it != null) MultiFieldValue(
                        id = it.fieldId,
                        `$type` = /*types[it.`$type`]*/ "jetbrains.charisma.customfields.complex.version.MultiVersionIssueCustomField",
                        value = listOf(ActualValue(id = it.id, name = it.name))
                ) else null
            }
            else -> dictionaries.customFieldValues.asSequence().firstOrNull {
                it.fieldName == fieldName && it.name == value && it.projectName == projectName
            }.let { it ->
                if (it != null) SingleFieldValue(
                        id = it.fieldId,
                        `$type` = types[it.`$type`],
                        value = ActualValue(id = it.id, name = it.name)
                ) else null
            }
        }
    }

    fun getCustomFieldListValue(fieldName: String, value: String?): FieldValueBase? {
        return when (fieldName) {
            "Assignee" -> dictionaries.users.firstOrNull { it.profile?.email?.email == value }.let { it ->
                if (it != null) SingleFieldValue(
                        id = "86-16",
                        `$type` = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField",
                        value = ActualValue(
                                id = it.id
                                        ?: "1-1", name = it.fullName ?: "admin"
                        )
                ) else null
            }
            else -> dictionaries.customFieldValues.asSequence().firstOrNull {
                it.fieldName == fieldName && it.name == value
            }.let {
                if (it != null) SingleFieldValue(
                        id = it.fieldId,
                        `$type` = types[it.`$type`],
                        value = ActualValue(id = it.id, name = it.name)
                ) else null
            }
        }
    }

    fun getPostableRequirement(item: TFSRequirement): String {
        println(item.id)
        val priority = getCustomFieldValue("W", "Priority", dictionaries.priorities[item.severity] ?: "Normal")
        val iterationPath = getCustomFieldValue("W", "Iteration", item.iterationPath ?: "\\P7\\PP9")
        val proposalQuality = getCustomFieldValue("W", "Proposal quality", item.proposalQuality ?: "Average")
        val type = getCustomFieldValue("W", "Type", item.type ?: "Requirement")
        val pmAccepted = getCustomFieldValue("W", "PM accepted", if (item.pmAccepted == "-1") "Yes" else "No")
        val dmAccepted = getCustomFieldValue("W", "DM accepted", if (item.dmAccepted == "-1") "Yes" else "No")
        val areaName = getCustomFieldValue("W", "Area name", item.areaName ?: "P7")
        val assignee = getCustomFieldValue("W", "Assignee", item.productManager)
        /*return Gson().toJson(
            YouTrackIssue(
                project = Project(id = "0-15"),
                summary = item.title,
                description = "TFS: ${item.id} \n\nPD:\n${Jsoup.parse(item.problemDescription).text()} \n\nPC:\n${Jsoup.parse(
                    item.proposedChange
                ).text()} \n\nER:\n${Jsoup.parse(item.expectedResult).text()}",
                fields = listOfNotNull(
                    priority,
                    type,
                    proposalQuality,
                    pmAccepted,
                    dmAccepted,
                    areaName,
                    assignee,
                    iterationPath
                )
            )
        )*/
        return ""
    }

    fun getPostableTask(item: TFSTask, iteration: String?): String {
        println(item)
        val type = getCustomFieldValue("W", "Type", item.type ?: "Task")
        val iterationPath = getCustomFieldValue("W", "Iteration", iteration ?: item.iterationPath ?: "\\P7\\PP9")
        val areaName = getCustomFieldValue("W", "Area name", item.areaName ?: "P7")
        val estimationDev = PeriodValue(
                id = "100-17",
                `$type` = "jetbrains.youtrack.timetracking.periodField.PeriodIssueCustomField",
                value = ActualPeriodValue(
                        `$type` = "jetbrains.youtrack.timetracking.periodField.PeriodValue",
                        minutes = (item.testEc + item.developmentEc) * 60
                )
        )
        val assignee = listOf(
                getCustomFieldValue("W", "Assignee", item.developer),
                getCustomFieldValue("W", "Assignee", item.tester)
        ).firstOrNull()
        /*return Gson().toJson(
            YouTrackIssue(
                project = Project(id = "0-15"),
                summary = item.title,
                description = "TFS: ${item.id} \n\n${Jsoup.parse(item.description).text()}",
                fields = listOfNotNull(type, areaName, estimationDev, assignee, iterationPath)
            )
        )*/
        return ""
    }

    override fun getBuildsByIteration(iteration: String): ResponseEntity<Any> {
        val statement = """
            SELECT
  DISTINCT changeRequest.Prognoz_P7_ChangeRequest_MergedIn AS build
FROM CurrentWorkItemView changeRequest
  LEFT JOIN vFactLinkedCurrentWorkItem links ON changeRequest.WorkItemSK = links.SourceWorkItemSK
  LEFT JOIN CurrentWorkItemView defect ON links.TargetWorkitemSK = defect.WorkItemSK
WHERE changeRequest.System_WorkItemType = 'Change Request'
      AND changeRequest.IterationPath = '$iteration'
      AND changeRequest.Prognoz_P7_ChangeRequest_MergedIn IS NOT NULL
ORDER BY changeRequest.Prognoz_P7_ChangeRequest_MergedIn DESC
      """
        val result = arrayListOf<String>()
        transaction(ms) {
            TransactionManager.current().exec(statement) { rs ->
                while (rs.next()) {
                    result.add(rs.getString("build"))
                }
                result
            }?.forEach { println(it) }
        }
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }

    override fun getIterations(): ResponseEntity<Any> {
        val statement = """SELECT DISTINCT IterationPath
FROM CurrentWorkItemView
where IterationPath LIKE '\P7\PP9\9.0%%'
      """
        val result = arrayListOf<String>()
        transaction(ms) {
            TransactionManager.current().exec(statement) { rs ->
                while (rs.next()) {
                    result.add(rs.getString("IterationPath"))
                }
                result
            }?.forEach { println(it) }
        }
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }

    override fun getDefectsByFixedBuildId(iteration: String, build: String): ResponseEntity<Any> {
        val statement = """SELECT
  changeRequest.System_Id                         AS CHANGE_REQUEST_ID,
  defect.System_Id                                AS PARENT_ID,
  defect.System_WorkItemType                      AS PARENT_TYPE,
  changeRequest.Prognoz_P7_ChangeRequest_MergedIn AS MERGED_IN,
  changeRequest.AreaName                          AS AREA_NAME,
  changeRequest.AreaPath                          AS AREA_PATH,
  defect.IterationPath                            AS ITERATION_PATH,
  defect.IterationName                            AS ITERATION_NAME,
  (SELECT TOP 1 Words
   FROM
     Tfs_DefaultCollection.dbo.WorkItemLongTexts t
   WHERE defect.System_Id = t.ID AND
         t.FldID = 1
   ORDER BY Rev DESC)                             AS TITLE,
  CASE defect.System_WorkItemType
  WHEN 'Defect'
    THEN
      (SELECT TOP 1 Words
       FROM
         Tfs_DefaultCollection.dbo.WorkItemLongTexts t
       WHERE defect.System_Id = t.ID AND
             t.FldID = 10228
       ORDER BY Rev DESC)
  WHEN 'Task'
    THEN (SELECT TOP 1 Words
          FROM
            Tfs_DefaultCollection.dbo.WorkItemLongTexts t
          WHERE defect.System_Id = t.ID AND
                t.FldID = 52
          ORDER BY Rev DESC)
  ELSE 'Undefined' END                            AS BODY
FROM CurrentWorkItemView changeRequest
  LEFT JOIN vFactLinkedCurrentWorkItem links ON changeRequest.WorkItemSK = links.SourceWorkItemSK
  LEFT JOIN CurrentWorkItemView defect ON links.TargetWorkitemSK = defect.WorkItemSK
WHERE changeRequest.System_WorkItemType = 'Change Request'
      AND changeRequest.IterationPath = '$iteration'
      AND changeRequest.Prognoz_P7_ChangeRequest_MergedIn in (${build.removeSurrounding("[", "]")})
      AND changeRequest.System_State = 'Closed'
      AND defect.System_WorkItemType IN ('Defect', 'Task')
      """
        val result = arrayListOf<TFSDefect>()
        transaction(ms) {
            TransactionManager.current().exec(statement) { rs ->
                /*val result = arrayListOf<Pair<String, String>>()*/

                while (rs.next()) {
                    val i = TFSDefect(
                            changeRequestId = rs.getString("CHANGE_REQUEST_ID").toInt(),
                            parentId = rs.getString("PARENT_ID").toInt(),
                            parentType = rs.getString("PARENT_TYPE"),
                            mergedIn = rs.getString("MERGED_IN").toInt(),
                            areaName = rs.getString("AREA_NAME"),
                            areaPath = rs.getString("AREA_PATH"),
                            iterationPath = rs.getString("ITERATION_PATH"),
                            iterationName = rs.getString("ITERATION_NAME"),
                            title = rs.getString("TITLE"),
                            body = Jsoup.parse(rs.getString("BODY")).text()
                    )
                    result.add(i)
                }
                result
            }?.forEach { println(it) }
        }
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }

    override fun postChangeRequestById(id: Int, body: String?): ResponseEntity<Any> {
        val statement = """SELECT
  changeRequest.System_Id                         AS CHANGE_REQUEST_ID,
  defect.System_Id                                AS PARENT_ID,
  defect.System_WorkItemType                      AS PARENT_TYPE,
  changeRequest.Prognoz_P7_ChangeRequest_MergedIn AS MERGED_IN,
  defect.AreaName                          AS AREA_NAME,
  defect.AreaPath                          AS AREA_PATH,
  changeRequest.IterationPath                            AS ITERATION_PATH,
  changeRequest.IterationName                            AS ITERATION_NAME,
  (SELECT TOP 1 Words
   FROM
     Tfs_DefaultCollection.dbo.WorkItemLongTexts t
   WHERE defect.System_Id = t.ID AND
         t.FldID = 1
   ORDER BY Rev DESC)                             AS TITLE,
  CASE defect.System_WorkItemType
  WHEN 'Defect'
    THEN
      (SELECT TOP 1 Words
       FROM
         Tfs_DefaultCollection.dbo.WorkItemLongTexts t
       WHERE defect.System_Id = t.ID AND
             t.FldID = 10228
       ORDER BY Rev DESC)
  WHEN 'Task'
    THEN (SELECT TOP 1 Words
          FROM
            Tfs_DefaultCollection.dbo.WorkItemLongTexts t
          WHERE defect.System_Id = t.ID AND
                t.FldID = 52
          ORDER BY Rev DESC)
  ELSE 'Undefined' END                            AS BODY
FROM CurrentWorkItemView changeRequest
  LEFT JOIN vFactLinkedCurrentWorkItem links ON changeRequest.WorkItemSK = links.SourceWorkItemSK
  LEFT JOIN CurrentWorkItemView defect ON links.TargetWorkitemSK = defect.WorkItemSK
WHERE changeRequest.System_WorkItemType = 'Change Request'
      AND changeRequest.System_Id = '$id'
      AND defect.System_WorkItemType IN ('Defect', 'Task')
      """
        val result = arrayListOf<TFSDefect>()
        transaction(ms) {
            TransactionManager.current().exec(statement) { rs ->
                while (rs.next()) {
                    val i = TFSDefect(
                            changeRequestId = rs.getString("CHANGE_REQUEST_ID").toInt(),
                            parentId = rs.getString("PARENT_ID").toInt(),
                            parentType = rs.getString("PARENT_TYPE"),
                            mergedIn = rs.getString("MERGED_IN").toInt(),
                            areaName = rs.getString("AREA_NAME"),
                            areaPath = rs.getString("AREA_PATH"),
                            iterationPath = rs.getString("ITERATION_PATH"),
                            iterationName = rs.getString("ITERATION_NAME"),
                            title = rs.getString("TITLE"),
                            body = Jsoup.parse(rs.getString("BODY")).text()
                    )
                    result.add(i)
                }
                result
            }?.forEach { println(it) }
        }
        val item = result.first().apply {
            val i = Gson().fromJson(body, LinkedTreeMap::class.java)
            /*if (i["title"] != null) this.title = i["title"].toString()
            if (i["body"] != null) this.body = i["body"].toString()*/
        }
        val id2 = YouTrackAPI.create().createIssue(AUTH, Gson().toJson(item.toYouTrackPostableIssue("FP"))).execute()
        val idReadable = Gson().fromJson(id2.body(), Issue::class.java)
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers())
                .body(idReadable)
    }

    fun TFSDefect.toYouTrackPostableIssue(queueId: String): YouTrackPostableIssue? {
        this.parentType = when (this.parentType) {
            "Defect" -> "Bug"
            "Task" -> "Feature"
            else -> "Консультация"
        }
        val projectId = dictionaries.projects.firstOrNull { it.shortName == queueId }?.id ?: return null
        val type = getCustomFieldValue(queueId, "Type", this.parentType ?: "Bug")
        val product = getCustomFieldValue(queueId, "Продукт", /*this.parentType ?:*/ "FP 9.0")
        val database = getCustomFieldValue(queueId, "СУБД", "Любая СУБД")
        val os = getCustomFieldValue(queueId, "Операционная система", "Любая ОС")
        val issue = getCustomFieldValue(queueId, "Issue", this.parentId.toString())
        val component = getCustomFieldValue(
                queueId,
                "Subsystem",
                dictionaries.areas[this.areaPath] ?: "8. Прочее"
        ) //TODO calculate value from this.areaName
        val affectedVersions =
                getCustomFieldValue(queueId, "Affected versions", "9.0.202") //TODO calculate value from defect
        val fixedInBuild =
                getCustomFieldValue(
                        queueId,
                        "Исправлено в версии",
                        "${dictionaries.buildPrefixes[this.iterationPath]}${this.mergedIn}${dictionaries.buildSuffixes[this.iterationPath]}"
                )

        val firstResponseSLA = getCustomFieldValue(queueId, "SLA по первому ответу", "Выполнен")
        val solutionSLA = getCustomFieldValue(queueId, "SLA по решению", "Выполнен")
        val characteristics = getCustomFieldValue(queueId, "Характеристика", "Функциональность")
        return YouTrackPostableIssue(
                project = YouTrackProject(id = projectId),
                summary = this.title,
                description = "TFS: ${this.changeRequestId} \n\nPD:\n${this.body}",
                fields = listOfNotNull(
                        type, /*areaName,*/
                        database,
                        os,
                        product,
                        component,
                        affectedVersions,
                        fixedInBuild,
                        firstResponseSLA,
                        solutionSLA,
                        characteristics,
                        issue
                )
        )
    }
}