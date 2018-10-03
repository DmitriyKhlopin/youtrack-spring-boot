package fsight.youtrack.etl

import fsight.youtrack.bundles.BundleService
import fsight.youtrack.issues.IssueService
import fsight.youtrack.models.ETLResult
import fsight.youtrack.projects.ProjectsService
import fsight.youtrack.timeline.IssuesTimelineService
import fsight.youtrack.users.UsersService
import org.springframework.stereotype.Service
import java.util.*

@Service
class ETLImplementation(private val projects: ProjectsService,
                        private val bundles: BundleService,
                        private val issue: IssueService,
                        private val users: UsersService,
                        private val timeline: IssuesTimelineService) : ETLService {
    override fun loadDataFromYT(): ETLResult {
        val h = GregorianCalendar.getInstance().also { it.time = Date() }.get(Calendar.HOUR_OF_DAY)
        if (h in listOf(8, 20)) projects.getProjects()
        if (h in listOf(8, 20)) bundles.getBundles()
        issue.getIssues()
        if (h in listOf(8, 20)) users.getUsers()
        if (h in listOf(8, 20)) {
            println("starting timeline calculation")
            timeline.start()
        }
        if (h in listOf(8, 20)) issue.checkIssues()
        println("completed ETL")
        return ETLResult(1, 1)
    }
}