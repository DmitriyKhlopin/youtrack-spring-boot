package fsight.youtrack.api.tfs

import fsight.youtrack.generated.jooq.tables.TfsWi.TFS_WI
import fsight.youtrack.models.TFSItem
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class TFSDataImplementation(private val dslContext: DSLContext) : TFSDataService {
    override fun getItemsCount(): Int {
        return dslContext.selectCount().from(TFS_WI).fetchOneInto(Int::class.java)
    }

    override fun getItems(): List<TFSItem> {
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
        ).from(TFS_WI).fetchInto(TFSItem::class.java)
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
        println(item)
        return item
    }
}