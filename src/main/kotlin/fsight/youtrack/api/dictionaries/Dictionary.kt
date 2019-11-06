package fsight.youtrack.api.dictionaries

import fsight.youtrack.generated.jooq.Tables
import fsight.youtrack.generated.jooq.tables.DevopsStatesOrder.DEVOPS_STATES_ORDER
import fsight.youtrack.models.sql.DevOpsStateOrder
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Dictionary(private val dsl: DSLContext) : IDictionary {
    override val commercialProjects: ArrayList<String> = arrayListOf()
    override val innerProjects: ArrayList<String> = arrayListOf()
    override val devOpsStates: ArrayList<DevOpsStateOrder> = arrayListOf()

    override fun preloadCommercialProjects() {
        commercialProjects.clear()
        val i = dsl.select(Tables.PROJECTS.SHORT_NAME)
                .from(Tables.PROJECTS)
                .leftJoin(Tables.PROJECT_TYPE).on(Tables.PROJECTS.SHORT_NAME.eq(Tables.PROJECT_TYPE.PROJECT_SHORT_NAME))
                .where(Tables.PROJECT_TYPE.IS_PUBLIC.eq(true).or(Tables.PROJECT_TYPE.IS_PUBLIC.isNull))
                .fetchInto(String::class.java)
        commercialProjects.addAll(i)
        println("${commercialProjects.size} commercial projects cached")
    }

    override fun preloadInnerProjects() {
        innerProjects.clear()
        val i = dsl.select(Tables.PROJECTS.SHORT_NAME)
                .from(Tables.PROJECTS)
                .leftJoin(Tables.PROJECT_TYPE).on(Tables.PROJECTS.SHORT_NAME.eq(Tables.PROJECT_TYPE.PROJECT_SHORT_NAME))
                .where(Tables.PROJECT_TYPE.IS_PUBLIC.eq(false))
                .fetchInto(String::class.java)
        innerProjects.addAll(i)
        println("${innerProjects.size} inner projects cached")
    }

    override fun preloadDevOpsStates() {
        devOpsStates.clear()
        val i = dsl.select(DEVOPS_STATES_ORDER.STATE.`as`("state"), DEVOPS_STATES_ORDER.ORD.`as`("order"))
                .from(DEVOPS_STATES_ORDER)
                .fetchInto(DevOpsStateOrder::class.java)
        devOpsStates.addAll(i)
        println("${devOpsStates.size} DevOps states cached")
    }
}
