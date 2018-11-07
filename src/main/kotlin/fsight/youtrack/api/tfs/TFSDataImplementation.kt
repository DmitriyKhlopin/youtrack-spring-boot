package fsight.youtrack.api.tfs

import com.google.gson.Gson
import fsight.youtrack.AUTH
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.etl.bundles.v2.BundleValue
import fsight.youtrack.generated.jooq.tables.BundleValues.BUNDLE_VALUES
import fsight.youtrack.generated.jooq.tables.TfsLinks.TFS_LINKS
import fsight.youtrack.generated.jooq.tables.TfsTasks.TFS_TASKS
import fsight.youtrack.generated.jooq.tables.TfsWi.TFS_WI
import fsight.youtrack.generated.jooq.tables.Users.USERS
import fsight.youtrack.models.TFSRequirement
import fsight.youtrack.models.UserDetails
import fsight.youtrack.models.v2.Project
import org.jooq.DSLContext
import org.jsoup.Jsoup
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.sql.Timestamp

@Service
class TFSDataImplementation(private val dslContext: DSLContext) : TFSDataService {
    private final val types = hashMapOf<String, String>()
    private final val customFieldValues = arrayListOf<BundleValue>()
    private final val users = arrayListOf<UserDetails>()

    init {
        types["jetbrains.charisma.customfields.complex.state.StateBundle"] = "jetbrains.charisma.customfields.complex.state.StateIssueCustomField"
        types["jetbrains.charisma.customfields.complex.ownedField.OwnedBundle"] = "jetbrains.charisma.customfields.complex.ownedField.SingleOwnedIssueCustomField"
        types["jetbrains.charisma.customfields.complex.enumeration.EnumBundle"] = "jetbrains.charisma.customfields.complex.enumeration.SingleEnumIssueCustomField"
        types["jetbrains.charisma.customfields.complex.version.VersionBundle"] = "jetbrains.charisma.customfields.complex.version.SingleVersionIssueCustomField"
        types["jetbrains.charisma.customfields.complex.user.UserBundle"] = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField"
        /*types["jetbrains.charisma.customfields.complex.user.UserBundle"] = "jetbrains.charisma.persistence.user.User"*/

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
                TFS_WI.CREATE_DATE.`as`("create_date"),
                TFS_WI.SEVERITY.`as`("severity"),
                TFS_WI.PROJECT.`as`("project"),
                TFS_WI.CUSTOMER.`as`("customer"),
                TFS_WI.PRODUCT_MANAGER.`as`("product_manager"),
                TFS_WI.PRODUCT_MANAGER_DIRECTOR.`as`("product_manager_director"),
                TFS_WI.PROPOSAL_QUALITY.`as`("proposal_quality"),
                TFS_WI.PM_ACCEPTED.`as`("pm_accepted"),
                TFS_WI.DM_ACCEPTED.`as`("dm_accepted"),
                TFS_WI.PROJECT_NODE_NAME.`as`("project_node_name"),
                TFS_WI.PROJECT_PATH.`as`("project_path"),
                TFS_WI.AREA_NAME.`as`("area_name"),
                TFS_WI.AREA_PATH.`as`("area_path"),
                TFS_WI.ITERATION_PATH.`as`("iteration_path"),
                TFS_WI.ITERATION_NAME.`as`("iteration_name"),
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
                TFS_WI.CREATE_DATE.`as`("create_date"),
                TFS_WI.SEVERITY.`as`("severity"),
                TFS_WI.PROJECT.`as`("project"),
                TFS_WI.CUSTOMER.`as`("customer"),
                TFS_WI.PRODUCT_MANAGER.`as`("product_manager"),
                TFS_WI.PRODUCT_MANAGER_DIRECTOR.`as`("product_manager_director"),
                TFS_WI.PROPOSAL_QUALITY.`as`("proposal_quality"),
                TFS_WI.PM_ACCEPTED.`as`("pm_accepted"),
                TFS_WI.DM_ACCEPTED.`as`("dm_accepted"),
                TFS_WI.PROJECT_NODE_NAME.`as`("project_node_name"),
                TFS_WI.PROJECT_PATH.`as`("project_path"),
                TFS_WI.AREA_NAME.`as`("area_name"),
                TFS_WI.AREA_PATH.`as`("area_path"),
                TFS_WI.ITERATION_PATH.`as`("iteration_path"),
                TFS_WI.ITERATION_NAME.`as`("iteration_name"),
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

        items.forEach {
            val item = getItemById(it)
            val postableItem = getPostableRequirement(item)
            println(postableItem)
            val id2 = PostIssueRetrofitService.create().createIssue(AUTH, postableItem).execute()
            /*println("readable id = ${id2.body()} - ${id2.errorBody()}")*/
            val idReadable = Gson().fromJson(id2.body(), ReadableId::class.java)

            if (id2.errorBody() == null) getTasks(it, idReadable.idReadable ?: "")
        }
        return ResponseEntity.status(HttpStatus.OK).body(items)
    }

    override fun postItemToYouTrack(id: Int): ResponseEntity<Any> {
        val item = getItemById(id)
        val postableItem = getPostableRequirement(item)
        println(postableItem)
        val id2 = PostIssueRetrofitService.create().createIssue(AUTH, postableItem).execute()
        println("readable id = ${id2.body()} - ${id2.errorBody()}")
        val idReadable = Gson().fromJson(id2.body(), ReadableId::class.java)

        if (id2.errorBody() == null) getTasks(id, idReadable.idReadable ?: "")
        return ResponseEntity.status(HttpStatus.OK).body(id2)
    }


    data class TFSTask(
            val id: Int,
            val rev: Int,
            val state: String? = null,
            val type: String? = null,
            val createDate: Timestamp,
            val discipline: String? = null,
            val developmentEc: Int,
            val testEc: Int,
            val project: String? = null,
            val projectNodeName: String? = null,
            val projectPath: String? = null,
            val areaName: String? = null,
            val areaPath: String? = null,
            val iterationPath: String? = null,
            val iterationName: String? = null,
            val title: String? = null,
            val description: String? = null,
            val developer: String? = null,
            val tester: String? = null
    )


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
            val idReadable = Gson().fromJson(id2.body(), ReadableId::class.java)
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
        /*val estimation = PeriodValue(id = "100-17", `$type` = "jetbrains.youtrack.timetracking.periodField.PeriodIssueCustomField", value = ActualPeriodValue(`$type` = "jetbrains.youtrack.timetracking.periodField.PeriodValue", minutes = 360))*/
        return Gson().toJson(
                IssueIn(
                        project = Project(id = "0-15"),
                        summary = item.title,
                        description = "TFS: ${item.id} \n\nPD:\n${Jsoup.parse(item.problemDescription).text()} \n\nPC:\n${Jsoup.parse(item.proposedChange).text()} \n\nER:\n${Jsoup.parse(item.expectedResult).text()}",
                        fields = arrayListOf(type, proposalQuality, pmAccepted, dmAccepted, areaName, assignee).filterNotNull() as ArrayList<Any>
                ))
    }

    fun getPostableTask(item: TFSTask): String {
        val type = customFieldValues.asSequence().filter {
            it.fieldName == "Type" && it.name == (item.type ?: "Task")
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
                        fields = arrayListOf(type, areaName, estimationDev, assignee).filterNotNull() as ArrayList<Any>
                ))
    }

    data class YouTrackCommand(var issues: ArrayList<IssueIn>, var silent: Boolean = false, var query: String = "")
    data class ReadableId(val id: String? = null, val idReadable: String? = null)
    data class ActualPeriodValue(val `$type`: String?, val minutes: Int?)
    data class PeriodValue(val id: String?, val `$type`: String?, val value: ActualPeriodValue?)
    data class ActualValue(val id: String?, val name: String?)
    data class FieldValue(val id: String?, val `$type`: String?, val value: ActualValue?)
    data class IssueIn(var description: String? = null, var fields: ArrayList<Any>? = null, var project: Project? = null, var summary: String? = null, var id: String? = null)
}

interface PostIssueRetrofitService {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("issues?fields=idReadable,id")
    fun createIssue(
            @Header("Authorization") auth: String,
            @Body model: String): Call<String>

    companion object Factory {
        fun create(): PostIssueRetrofitService {
            val retrofit = Retrofit.Builder().baseUrl(NEW_ROOT_REF).addConverterFactory(ScalarsConverterFactory.create()).build()
            return retrofit.create(PostIssueRetrofitService::class.java)
        }
    }
}

interface PostCommandService {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("commands")
    fun createIssue(
            @Header("Authorization") auth: String,
            @Body model: String): Call<String>

    companion object Factory {
        fun create(): PostCommandService {
            val retrofit = Retrofit.Builder().baseUrl(NEW_ROOT_REF).addConverterFactory(ScalarsConverterFactory.create()).build()
            return retrofit.create(PostCommandService::class.java)
        }
    }
}