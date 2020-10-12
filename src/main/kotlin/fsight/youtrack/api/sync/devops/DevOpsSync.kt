package fsight.youtrack.api.sync.devops

import com.google.gson.Gson
import fsight.youtrack.DEVOPS_AUTH
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.common.IResolver
import fsight.youtrack.db.IDevOpsProvider
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.getBugsAndFeatures
import fsight.youtrack.models.youtrack.Command
import fsight.youtrack.models.youtrack.Issue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class DevOpsSync : IDevOpsSync {
    @Autowired
    private lateinit var pg: IPGProvider

    @Autowired
    private lateinit var devOps: IDevOpsProvider

    @Autowired
    private lateinit var issueService: IIssue

    @Autowired
    private lateinit var resolver: IResolver

    override fun syncUnresolved(): Any {
        val ids = pg.getIssuesIdsInDevelopment()
        val issues = ArrayList<Issue>()
        val t1 = measureTimeMillis {
            ids.forEach { id ->
                val filter = "#$id и Состояние: {Направлена разработчику}"
                val i = issueService.search(filter, listOf("idReadable", "customFields(name,value(name))"))
                issues.addAll(i)
            }
        }
        val wiIds = issues.getBugsAndFeatures()

        val wis = devOps.getDevOpsItemsByIds(wiIds)/*.filter { it.state !in listOf("Closed", "Resolved") }*/
        println(t1)

        /*val teams = wis.map { it.systemId to "${it.area} -  ${resolver.resolveAreaToTeam(it.area ?: "")}" }*/
        val filteredIssues = issues.filter { issue ->
            val team = wis.firstOrNull { wi -> issue.bugsAndFeatures().sortedBy { it }.contains(wi.systemId) }.let { wi -> resolver.resolveAreaToTeam(wi?.area ?: "") }
            issue.unwrapEnumValue("Команда") != team
        }.map { issue -> issue.idReadable to wis.firstOrNull { wi -> issue.bugsAndFeatures().sortedBy { it }.contains(wi.systemId) }.let { wi -> resolver.resolveAreaToTeam(wi?.area ?: "") } }

        filteredIssues.forEach {
            if (it.first != null && it.second != null) {
                postCommand(it.first, "Команда ${it.second}")
            }
        }
        return filteredIssues
    }

    fun postCommand(id: String?, command: String): Any {
        val cmd = Gson().toJson(Command(issues = arrayListOf(Issue(idReadable = id)), query = command))
        val response = YouTrackAPI.create().postCommand(DEVOPS_AUTH, cmd).execute()
        return response.code()
    }
}
