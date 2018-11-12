package fsight.youtrack.api.tfs

import com.google.gson.Gson
import fsight.youtrack.AUTH
import fsight.youtrack.api.etl.bundles.v2.BundleValue
import fsight.youtrack.generated.jooq.tables.BundleValues.BUNDLE_VALUES
import fsight.youtrack.generated.jooq.tables.TfsLinks.TFS_LINKS
import fsight.youtrack.generated.jooq.tables.TfsTasks.TFS_TASKS
import fsight.youtrack.generated.jooq.tables.TfsWi.TFS_WI
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.models.TFSRequirement
import fsight.youtrack.models.TFSTask
import fsight.youtrack.models.UserDetails
import fsight.youtrack.models.YouTrackCommand
import fsight.youtrack.models.v2.Project
import org.jooq.DSLContext
import org.jsoup.Jsoup
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class TFSDataImplementation(private val dslContext: DSLContext) : TFSDataService {
    private final val types = hashMapOf<String, String>()
    private final val customFieldValues = arrayListOf<BundleValue>()
    private final val users = arrayListOf<UserDetails>()
    private final val prioritiesMap: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also { it ->
            it["High"] = "Major"
            it["Medium"] = "Normal"
            it["Low"] = "Minor"
        }
    }

    init {
        types["jetbrains.charisma.customfields.complex.state.StateBundle"] = "jetbrains.charisma.customfields.complex.state.StateIssueCustomField"
        types["jetbrains.charisma.customfields.complex.ownedField.OwnedBundle"] = "jetbrains.charisma.customfields.complex.ownedField.SingleOwnedIssueCustomField"
        types["jetbrains.charisma.customfields.complex.enumeration.EnumBundle"] = "jetbrains.charisma.customfields.complex.enumeration.SingleEnumIssueCustomField"
        types["jetbrains.charisma.customfields.complex.version.VersionBundle"] = "jetbrains.charisma.customfields.complex.version.SingleVersionIssueCustomField"
        types["jetbrains.charisma.customfields.complex.user.UserBundle"] = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField"
        /*types["jetbrains.charisma.customfields.complex.user.UserBundle"] = "jetbrains.charisma.persistence.user.User"*/
        this.initDictionaries()
    }

    override fun initDictionaries() {
        customFieldValues.clear()
        users.clear()
        customFieldValues.addAll(
                dslContext
                        .select(
                                BUNDLE_VALUES.ID.`as`("id"),
                                BUNDLE_VALUES.NAME.`as`("name"),
                                BUNDLE_VALUES.PROJECT_ID.`as`("projectId"),
                                BUNDLE_VALUES.PROJECT_NAME.`as`("projectName"),
                                BUNDLE_VALUES.FIELD_ID.`as`("fieldId"),
                                BUNDLE_VALUES.FIELD_NAME.`as`("fieldName"),
                                BUNDLE_VALUES.TYPE.`as`("\$type"))
                        .from(BUNDLE_VALUES)
                        .where(BUNDLE_VALUES.PROJECT_ID.eq("0-15"))
                        .fetchInto(BundleValue::class.java)
        )

        users.addAll(
                dslContext
                        .select(USERS.ID.`as`("id"),
                                USERS.FULL_NAME.`as`("fullName"),
                                USERS.EMAIL.`as`("email"))
                        .from(USERS)
                        .where(USERS.EMAIL.isNotNull)
                        .fetchInto(UserDetails::class.java)
        )
    }


    override fun getItemsCount(): Int {
        return dslContext.selectCount().from(TFS_WI).fetchOneInto(Int::class.java)
    }

    override fun getItems(offset: Int?, limit: Int?): ResponseEntity<Any> {
        val items = dslContext.select(
                TFS_WI.ID.`as`("id"),
                TFS_WI.REV.`as`("rev"),
                TFS_WI.STATE.`as`("state"),
                TFS_WI.TYPE.`as`("type"),
                TFS_WI.CREATE_DATE.`as`("createDate"),
                TFS_WI.SEVERITY.`as`("severity"),
                TFS_WI.PROJECT.`as`("project"),
                TFS_WI.CUSTOMER.`as`("customer"),
                TFS_WI.PRODUCT_MANAGER.`as`("productManager"),
                TFS_WI.PRODUCT_MANAGER_DIRECTOR.`as`("productManagerDirector"),
                TFS_WI.PROPOSAL_QUALITY.`as`("proposalQuality"),
                TFS_WI.PM_ACCEPTED.`as`("pmAccepted"),
                TFS_WI.DM_ACCEPTED.`as`("dmAccepted"),
                TFS_WI.PROJECT_NODE_NAME.`as`("projectNodeName"),
                TFS_WI.PROJECT_PATH.`as`("projectPath"),
                TFS_WI.AREA_NAME.`as`("areaName"),
                TFS_WI.AREA_PATH.`as`("areaPath"),
                TFS_WI.ITERATION_PATH.`as`("iterationPath"),
                TFS_WI.ITERATION_NAME.`as`("iterationName"),
                TFS_WI.TITLE.`as`("title"),
                TFS_WI.PROBLEM_DESCRIPTION.`as`("problemDescription"),
                TFS_WI.PROPOSED_CHANGE.`as`("proposedChange"),
                TFS_WI.EXPECTED_RESULT.`as`("expectedResult")
        )
                .from(TFS_WI)
                .orderBy(TFS_WI.ID)
                .limit(limit ?: getItemsCount())
                .offset(offset ?: 0)
                .fetchInto(TFSRequirement::class.java)
        return ResponseEntity.status(HttpStatus.OK).body(items)
    }

    override fun getItemById(id: Int): TFSRequirement {
        return dslContext.select(
                TFS_WI.ID.`as`("id"),
                TFS_WI.REV.`as`("rev"),
                TFS_WI.STATE.`as`("state"),
                TFS_WI.TYPE.`as`("type"),
                TFS_WI.CREATE_DATE.`as`("createDate"),
                TFS_WI.SEVERITY.`as`("severity"),
                TFS_WI.PROJECT.`as`("project"),
                TFS_WI.CUSTOMER.`as`("customer"),
                TFS_WI.PRODUCT_MANAGER.`as`("productManager"),
                TFS_WI.PRODUCT_MANAGER_DIRECTOR.`as`("productManagerDirector"),
                TFS_WI.PROPOSAL_QUALITY.`as`("proposalQuality"),
                TFS_WI.PM_ACCEPTED.`as`("pmAccepted"),
                TFS_WI.DM_ACCEPTED.`as`("dmAccepted"),
                TFS_WI.PROJECT_NODE_NAME.`as`("projectNodeName"),
                TFS_WI.PROJECT_PATH.`as`("projectPath"),
                TFS_WI.AREA_NAME.`as`("areaName"),
                TFS_WI.AREA_PATH.`as`("areaPath"),
                TFS_WI.ITERATION_PATH.`as`("iterationPath"),
                TFS_WI.ITERATION_NAME.`as`("iterationName"),
                TFS_WI.TITLE.`as`("title"),
                TFS_WI.PROBLEM_DESCRIPTION.`as`("problemDescription"),
                TFS_WI.PROPOSED_CHANGE.`as`("proposedChange"),
                TFS_WI.EXPECTED_RESULT.`as`("expectedResult")
        ).from(TFS_WI).where(TFS_WI.ID.eq(id)).limit(1).fetchOneInto(TFSRequirement::class.java)
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
        initDictionaries()
        val items = dslContext.select(
                TFS_WI.ID.`as`("id"),
                TFS_WI.REV.`as`("rev"),
                TFS_WI.CREATE_DATE.`as`("createDate")
        )
                .from(TFS_WI)
                .where(TFS_WI.ITERATION_PATH.eq(iteration))
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
        val id2 = PostIssueRetrofitService.create().createIssue(AUTH, postableItem).execute()
        val idReadable = Gson().fromJson(id2.body(), IssueIn::class.java)
        if (id2.errorBody() == null) getTasks(id, idReadable.idReadable ?: "")
    }


    fun getTasks(requirementId: Int, parentId: String) {
        val r = dslContext.select(
                TFS_TASKS.ID,
                TFS_TASKS.REV,
                TFS_TASKS.STATE,
                TFS_TASKS.TYPE,
                TFS_TASKS.CREATE_DATE.`as`("createDate"),
                TFS_TASKS.DISCIPLINE,
                TFS_TASKS.DEVELOPMENT_EC.`as`("developmentEc"),
                TFS_TASKS.TEST_EC.`as`("testEc"),
                TFS_TASKS.PROJECT,
                TFS_TASKS.PROJECT_NODE_NAME.`as`("projectNodeName"),
                TFS_TASKS.PROJECT_PATH.`as`("projectPath"),
                TFS_TASKS.AREA_NAME.`as`("areaName"),
                TFS_TASKS.AREA_PATH.`as`("areaPath"),
                TFS_TASKS.ITERATION_PATH.`as`("iterationPath"),
                TFS_TASKS.ITERATION_NAME.`as`("iterationName"),
                TFS_TASKS.TITLE,
                TFS_TASKS.DESCRIPTION,
                TFS_TASKS.DEVELOPER,
                TFS_TASKS.TESTER
        )
                .from(TFS_TASKS)
                .leftJoin(TFS_LINKS).on(TFS_TASKS.ID.eq(TFS_LINKS.TARGET_ID))
                .where(TFS_LINKS.SOURCE_ID.eq(requirementId))
                .fetchInto(TFSTask::class.java).map { item -> getPostableTask(item) }
        r.forEach {
            /*println(it)*/
            val id2 = PostIssueRetrofitService.create().createIssue(AUTH, it).execute()
            println("readable id = ${id2.body()} - ${id2.errorBody()}")
            if (id2.errorBody() != null) println(it)
            val idReadable = Gson().fromJson(id2.body(), IssueIn::class.java)
            val command = Gson().toJson(YouTrackCommand(issues = arrayListOf(IssueIn(id = idReadable.id)), silent = true, query = "подзадача $parentId"))
            println(command)
            val r2 = PostCommandService.create().createIssue(AUTH, command).execute()
            println("readable id = ${r2.body()} - ${r2.errorBody()}")
        }
    }


    override fun toJson(id: Int): ResponseEntity<Any> {
        val item = getItemById(id)
        val postableItem = getPostableRequirement(item)
        return ResponseEntity.status(HttpStatus.OK).body(postableItem)
    }

    fun getPostableRequirement(item: TFSRequirement): String {
        println(item.id)
        val priority = customFieldValues.asSequence().filter {
            it.fieldName == "Priority" && it.name == (prioritiesMap[item.severity ?: "Medium"])
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val iterationPath = customFieldValues.asSequence().filter {
            it.fieldName == "Iteration" && it.name == (item.iterationPath ?: "\\P7\\PP9")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val proposalQuality = customFieldValues.asSequence().filter {
            it.fieldName == "Proposal quality" && it.name == (item.proposalQuality ?: "Average")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val type = customFieldValues.asSequence().filter {
            it.fieldName == "Type" && it.name == (item.type ?: "Requirement")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val pmAccepted = customFieldValues.asSequence().filter {
            it.fieldName == "PM accepted" && it.name == (if (item.pmAccepted == "-1") "Yes" else "No")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val dmAccepted = customFieldValues.asSequence().filter {
            it.fieldName == "DM accepted" && it.name == (if (item.dmAccepted == "-1") "Yes" else "No")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val areaName = customFieldValues.asSequence().filter {
            it.fieldName == "Area name" && it.name == item.areaName
        }.firstOrNull().let { it -> if (it != null) FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) else null }
        val u = users.firstOrNull { it.email == item.productManager }
        val assignee = FieldValue(id = "86-16", `$type` = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField", value = ActualValue(id = u?.id
                ?: "1-1", name = u?.fullName ?: "admin"))
        return Gson().toJson(
                IssueIn(
                        project = Project(id = "0-15"),
                        summary = item.title,
                        description = "TFS: ${item.id} \n\nPD:\n${Jsoup.parse(item.problemDescription).text()} \n\nPC:\n${Jsoup.parse(item.proposedChange).text()} \n\nER:\n${Jsoup.parse(item.expectedResult).text()}",
                        fields = listOfNotNull(priority, type, proposalQuality, pmAccepted, dmAccepted, areaName, assignee, iterationPath)
                ))
    }

    fun getPostableTask(item: TFSTask): String {
        println(item)
        val type = customFieldValues.asSequence().filter {
            it.fieldName == "Type" && it.name == (item.type ?: "Task")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val iterationPath = customFieldValues.asSequence().filter {
            it.fieldName == "Iteration" && it.name == (item.iterationPath ?: "\\P7\\PP9")
        }.first().let { it -> FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) }
        val areaName = customFieldValues.asSequence().filter {
            it.fieldName == "Area name" && it.name == item.areaName
        }.firstOrNull().let { it -> if (it !== null) FieldValue(id = it.fieldId, `$type` = types[it.`$type`], value = ActualValue(id = it.id, name = it.name)) else null }
        val estimationDev = PeriodValue(id = "100-17", `$type` = "jetbrains.youtrack.timetracking.periodField.PeriodIssueCustomField", value = ActualPeriodValue(`$type` = "jetbrains.youtrack.timetracking.periodField.PeriodValue", minutes = (item.testEc + item.developmentEc) * 60))
        val u = users.firstOrNull { it.email == item.developer || it.email == item.tester }
        val assignee = FieldValue(id = "86-16", `$type` = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField", value = ActualValue(id = u?.id
                ?: "1-1", name = u?.fullName ?: "admin"))
        return Gson().toJson(
                IssueIn(
                        project = Project(id = "0-15"),
                        summary = item.title,
                        description = "TFS: ${item.id} \n\n${Jsoup.parse(item.description).text()}",
                        fields = listOfNotNull(type, areaName, estimationDev, assignee, iterationPath)
                ))
    }

    /*data class ReadableId(val id: String? = null, val idReadable: String? = null)*/
    data class ActualPeriodValue(val `$type`: String?, val minutes: Int?)

    data class PeriodValue(val id: String?, val `$type`: String?, val value: ActualPeriodValue?)
    data class ActualValue(val id: String?, val name: String?)
    data class FieldValue(val id: String?, val `$type`: String?, val value: ActualValue?)
    data class IssueIn(var description: String? = null, var fields: List<Any>? = null, var project: Project? = null, var summary: String? = null, var id: String? = null, var idReadable: String? = null)
}