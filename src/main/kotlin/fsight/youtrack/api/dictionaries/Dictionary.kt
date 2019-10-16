package fsight.youtrack.api.dictionaries

import fsight.youtrack.generated.jooq.Tables
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Dictionary(private val dsl: DSLContext) : IDictionary {
    override val commercialProjects: ArrayList<String> = arrayListOf()

    override val innerProjects: ArrayList<String> = arrayListOf()


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
}
