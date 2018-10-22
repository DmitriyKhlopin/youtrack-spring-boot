package fsight.youtrack.api.tfs

import com.google.gson.Gson
import fsight.youtrack.AUTH
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.generated.jooq.tables.TfsWi.TFS_WI
import fsight.youtrack.models.TFSItem
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


@Service
class TFSDataImplementation(private val dslContext: DSLContext) : TFSDataService {
    override fun getItemsCount(): Int {
        return dslContext.selectCount().from(TFS_WI).fetchOneInto(Int::class.java)
    }

    override fun getItems(offset: Int?, limit: Int?): List<TFSItem> {
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
                TFS_WI.ITERATION_NAME.`as`("iteration_name")
        )
                .from(TFS_WI)
                .orderBy(TFS_WI.ID)
                .limit(limit ?: getItemsCount())
                .offset(offset ?: 0)
                .fetchInto(TFSItem::class.java)
    }

    override fun getItemById(id: Int): TFSItem {
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
                TFS_WI.ITERATION_NAME.`as`("iteration_name")
        ).from(TFS_WI).where(TFS_WI.ID.eq(id)).fetchOneInto(TFSItem::class.java)
    }

    override fun postItemToYouTrack(id: Int): TFSItem {
        val item = dslContext.select(
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
                TFS_WI.ITERATION_NAME.`as`("iterationName")
        ).from(TFS_WI).where(TFS_WI.ID.eq(id)).fetchOneInto(TFSItem::class.java)

        val postableItem = Gson().toJson(
                YTIssue(
                        project = Project("0-15"),
                        summary = "test",
                        description = "test"/*,
                        pmAccepted = "Yes"*/)
        )
        println(postableItem)
        val id2 = PostIssueRetrofitService.create().createIssue(AUTH, postableItem).execute()
        println("readable id = ${id2.body()} - ${id2.errorBody()}")
        return item
    }
}

/*@SerializedName("PM accepted")*/

data class Project(val id: String)
/*data class IssueCustomField(

)*/
data class YTIssue(
        val project: Project,
        val summary: String,
        val description: String
        /*val fields: ArrayList<>*/
        /*val pmAccepted: String,
        val priority:*/
)

interface PostIssueRetrofitService {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("issues?fields=idReadable")
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