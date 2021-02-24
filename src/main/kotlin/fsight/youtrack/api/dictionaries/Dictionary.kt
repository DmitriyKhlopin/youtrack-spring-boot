package fsight.youtrack.api.dictionaries

import fsight.youtrack.db.IPGProvider
import fsight.youtrack.db.models.devops.DevOpsStateOrder
import fsight.youtrack.etl.projects.IProjects
import fsight.youtrack.generated.jooq.tables.BundleValues.BUNDLE_VALUES
import fsight.youtrack.generated.jooq.tables.DevopsStatesOrder.DEVOPS_STATES_ORDER
import fsight.youtrack.generated.jooq.tables.IssueTags.ISSUE_TAGS
import fsight.youtrack.models.BundleValue
import fsight.youtrack.models.YouTrackProject
import fsight.youtrack.models.YouTrackUser
import fsight.youtrack.models.web.ReactSelectOption
import fsight.youtrack.toStartOfDate
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.PostConstruct

@Service
class Dictionary(private val dsl: DSLContext) : IDictionary {
    @Autowired
    private lateinit var pg: IPGProvider

    @Autowired
    private lateinit var projectsService: IProjects

    override val areas: HashMap<String, String> = HashMap()
    override val productOwners: HashMap<String, String> = HashMap()
    override val commercialProjects: ArrayList<ReactSelectOption> = arrayListOf()
    override val innerProjects: ArrayList<String> = arrayListOf()
    override val devOpsStates: ArrayList<DevOpsStateOrder> = arrayListOf()
    override val priorities: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also {
            it["Критичный"] = "Критичный"
            it["High"] = "Major"
            it["Medium"] = "Normal"
            it["Low"] = "Minor"
        }
    }

    override val holidays: ArrayList<LocalDate> = arrayListOf()
    override val extraWorkDays: ArrayList<LocalDate> = arrayListOf()


    override val buildPrefixes: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also { it["\\P7\\PP9\\9.0\\1.0\\Update 1"] = "9.0." }
    }

    override val buildSuffixes: HashMap<String, String> by lazy {
        hashMapOf<String, String>().also { it["\\P7\\PP9\\9.0\\1.0\\Update 1"] = ".June" }
    }

    override val projects: List<YouTrackProject> by lazy { projectsService.getProjects() }
    override val users: List<YouTrackUser> by lazy {
        /*dsl.select(Users.USERS.ID.`as`("id"), Users.USERS.FULL_NAME.`as`("fullName"), Users.USERS.EMAIL.`as`("email"))
            .from(Users.USERS)
            .where(Users.USERS.EMAIL.isNotNull)
            .fetchInto(YouTrackUser::class.java)*/
        listOf<YouTrackUser>()
    }

    override val customFieldValues: List<BundleValue> by lazy {
        dsl
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
            .fetchInto(BundleValue::class.java)
        listOf<BundleValue>()
    }

    override val sprints: HashMap<String, Pair<Timestamp, Timestamp>> by lazy {
        hashMapOf<String, Pair<Timestamp, Timestamp>>().also {
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 9"] = Pair("2019-11-25".toStartOfDate(), "2019-12-06".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 10"] = Pair("2019-12-09".toStartOfDate(), "2019-12-20".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 11"] = Pair("2019-12-23".toStartOfDate(), "2020-01-10".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 12"] = Pair("2020-01-13".toStartOfDate(), "2020-01-24".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 13"] = Pair("2020-01-27".toStartOfDate(), "2020-02-07".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 14"] = Pair("2020-02-10".toStartOfDate(), "2020-02-21".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 15"] = Pair("2020-02-24".toStartOfDate(), "2020-03-06".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 16"] = Pair("2020-03-09".toStartOfDate(), "2020-03-20".toStartOfDate())
            it["\\AP\\Backlog\\Q3 FY19\\Sprint 17"] = Pair("2020-03-23".toStartOfDate(), "2020-04-03".toStartOfDate())
        }
    }


    override fun loadCommercialProjects() {
        commercialProjects.clear()
        val i = pg.getCommercialProjects().map { ReactSelectOption(it.shortName ?: "", it.name ?: "", "#00B8D9") }
        commercialProjects.addAll(i)
        println("${commercialProjects.size} commercial projects cached")
    }

    override fun loadInnerProjects() {
        innerProjects.clear()
        val i = pg.getInnerProjects().mapNotNull { it.name }
        innerProjects.addAll(i)
        println("${innerProjects.size} inner projects cached")
    }

    override fun loadDevOpsStates() {
        devOpsStates.clear()
        val i = dsl.select(DEVOPS_STATES_ORDER.STATE.`as`("state"), DEVOPS_STATES_ORDER.ORD.`as`("order"))
            .from(DEVOPS_STATES_ORDER)
            .fetchInto(DevOpsStateOrder::class.java)
        devOpsStates.addAll(i)
        println("${devOpsStates.size} DevOps states cached")
    }

    override fun getTags(): List<String> {
        return dsl.selectDistinct(ISSUE_TAGS.TAG).from(ISSUE_TAGS).fetchInto(String::class.java)
    }

    override fun getPartnerCustomers(): Any {
        return pg.getPartnerCustomers()
    }

    override fun loadAreasWithTeams() {
        pg.getAreasWithTeams().forEach { areas[it.area] = it.team }
        println("${areas.size} areas cached")
    }

    override fun loadProductOwners() {
        pg.getProductOwners().forEach { productOwners[it.team] = it.owner }
        println("${productOwners.size} product owners cached")
    }

    override fun loadHolidays() {
        holidays.clear()
        val i = pg.getHolidays().map { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
        holidays.addAll(i)
        println("${holidays.size} holidays cached")
    }

    override fun loadExtraWorkDays() {
        extraWorkDays.clear()
        val i = pg.getExtraWorkDays().map { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
        extraWorkDays.addAll(i)
        println("${extraWorkDays.size} extra work days cached")
    }

    override fun loadAll() {
        loadDevOpsStates()
        loadCommercialProjects()
        loadInnerProjects()
        loadAreasWithTeams()
        loadProductOwners()
        loadHolidays()
        loadExtraWorkDays()
    }

    @PostConstruct
    fun init() {
        loadAll()
    }

}
