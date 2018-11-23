package fsight.youtrack.api.tfs

import com.google.gson.Gson
import fsight.youtrack.AUTH
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.api.etl.bundles.BundleValue

import fsight.youtrack.generated.jooq.tables.BundleValues.BUNDLE_VALUES
import fsight.youtrack.generated.jooq.tables.TfsLinks.TFS_LINKS
import fsight.youtrack.generated.jooq.tables.TfsTasks.TFS_TASKS
import fsight.youtrack.generated.jooq.tables.TfsWi.TFS_WI
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.models.*
import fsight.youtrack.models.v2.Project
import org.jooq.DSLContext
import org.jsoup.Jsoup
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class TFSData(private val dslContext: DSLContext) : ITFSData {
    private final val types: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also {
            it["jetbrains.charisma.customfields.complex.state.StateBundle"] =
                    "jetbrains.charisma.customfields.complex.state.StateIssueCustomField"
            it["jetbrains.charisma.customfields.complex.ownedField.OwnedBundle"] =
                    "jetbrains.charisma.customfields.complex.ownedField.SingleOwnedIssueCustomField"
            it["jetbrains.charisma.customfields.complex.enumeration.EnumBundle"] =
                    "jetbrains.charisma.customfields.complex.enumeration.SingleEnumIssueCustomField"
            it["jetbrains.charisma.customfields.complex.version.VersionBundle"] =
                    "jetbrains.charisma.customfields.complex.version.SingleVersionIssueCustomField"
            it["jetbrains.charisma.customfields.complex.user.UserBundle"] =
                    "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField"
        }
    }
    private final val prioritiesMap: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also { it ->
            it["High"] = "Major"
            it["Medium"] = "Normal"
            it["Low"] = "Minor"
        }
    }
    private final val customFieldValues = arrayListOf<BundleValue>()
    private final val users = arrayListOf<UserDetails>()

    init {
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
                    BUNDLE_VALUES.TYPE.`as`("\$type")
                )
                .from(BUNDLE_VALUES)
                .where(BUNDLE_VALUES.PROJECT_ID.eq("0-15"))
                .fetchInto(BundleValue::class.java)
        )

        users.addAll(
            dslContext
                .select(
                    USERS.ID.`as`("id"),
                    USERS.FULL_NAME.`as`("fullName"),
                    USERS.EMAIL.`as`("email")
                )
                .from(USERS)
                .where(USERS.EMAIL.isNotNull)
                .fetchInto(UserDetails::class.java)
        )
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
        initDictionaries()
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
        val id2 = YouTrackAPI.create().createIssue(AUTH, postableItem).execute()
        val idReadable = Gson().fromJson(id2.body(), YouTrackIssue::class.java)
        if (id2.errorBody() == null) getTasks(requirement = item, parentId = idReadable.idReadable ?: "")
    }


    fun getTasks(requirement: TFSRequirement, parentId: String) {
        val r = dslContext.select()
            .from(TFS_TASKS)
            .leftJoin(TFS_LINKS).on(TFS_TASKS.ID.eq(TFS_LINKS.TARGET_ID))
            .where(TFS_LINKS.SOURCE_ID.eq(requirement.id))
            .fetchInto(TFSTask::class.java).map { item -> getPostableTask(item, requirement.iterationPath) }
        r.forEach {
            val id2 = YouTrackAPI.create().createIssue(AUTH, it).execute()
            if (id2.errorBody() != null) println(it)
            val idReadable = Gson().fromJson(id2.body(), YouTrackIssue::class.java)
            val command = Gson().toJson(
                YouTrackCommand(
                    issues = arrayListOf(YouTrackIssue(id = idReadable.id)),
                    silent = true,
                    query = "подзадача $parentId"
                )
            )
            YouTrackAPI.create().postCommand(AUTH, command).execute()
        }
    }


    override fun toJson(id: Int): ResponseEntity<Any> {
        val item = getItemById(id)
        val postableItem = getPostableRequirement(item)
        return ResponseEntity.status(HttpStatus.OK).body(postableItem)
    }

    fun getCustomFieldValue(fieldName: String, value: String?): FieldValue? {
        return when (fieldName) {
            "Assignee" -> users.firstOrNull { it.email == value }.let { it ->
                if (it != null) FieldValue(
                    id = "86-16",
                    `$type` = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField",
                    value = ActualValue(
                        id = it.id
                            ?: "1-1", name = it.fullName ?: "admin"
                    )
                ) else null
            }
            else -> customFieldValues.asSequence().firstOrNull {
                it.fieldName == fieldName && it.name == value
            }.let { it ->
                if (it != null) FieldValue(
                    id = it.fieldId,
                    `$type` = types[it.`$type`],
                    value = ActualValue(id = it.id, name = it.name)
                ) else null
            }
        }
    }

    fun getPostableRequirement(item: TFSRequirement): String {
        println(item.id)
        val priority = getCustomFieldValue("Priority", prioritiesMap[item.severity] ?: "Normal")
        val iterationPath = getCustomFieldValue("Iteration", item.iterationPath ?: "\\P7\\PP9")
        val proposalQuality = getCustomFieldValue("Proposal quality", item.proposalQuality ?: "Average")
        val type = getCustomFieldValue("Type", item.type ?: "Requirement")
        val pmAccepted = getCustomFieldValue("PM accepted", if (item.pmAccepted == "-1") "Yes" else "No")
        val dmAccepted = getCustomFieldValue("DM accepted", if (item.dmAccepted == "-1") "Yes" else "No")
        val areaName = getCustomFieldValue("Area name", item.areaName ?: "P7")
        val assignee = getCustomFieldValue("Assignee", item.productManager)
        return Gson().toJson(
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
        )
    }

    fun getPostableTask(item: TFSTask, iteration: String?): String {
        println(item)
        val type = getCustomFieldValue("Type", item.type ?: "Task")
        val iterationPath = getCustomFieldValue("Iteration", iteration ?: item.iterationPath ?: "\\P7\\PP9")
        val areaName = getCustomFieldValue("Area name", item.areaName ?: "P7")
        val estimationDev = PeriodValue(
            id = "100-17",
            `$type` = "jetbrains.youtrack.timetracking.periodField.PeriodIssueCustomField",
            value = ActualPeriodValue(
                `$type` = "jetbrains.youtrack.timetracking.periodField.PeriodValue",
                minutes = (item.testEc + item.developmentEc) * 60
            )
        )
        val assignee = listOf(
            getCustomFieldValue("Assignee", item.developer),
            getCustomFieldValue("Assignee", item.tester)
        ).firstOrNull()
        return Gson().toJson(
            YouTrackIssue(
                project = Project(id = "0-15"),
                summary = item.title,
                description = "TFS: ${item.id} \n\n${Jsoup.parse(item.description).text()}",
                fields = listOfNotNull(type, areaName, estimationDev, assignee, iterationPath)
            )
        )
    }
}