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
    override fun loadDataFromYT(manual: Boolean): ETLResult {
        val m = GregorianCalendar.getInstance().also { it.time = Date() }.get(Calendar.MINUTE)
        if (m == 30) projects.getProjects()
        if (m == 30) bundles.getBundles()
        issue.getIssues()
        if (m == 30) users.getUsers()
        if (m == 30) timeline.start()
        if (m == 30) issue.checkIssues()
        println("completed ETL")
        return ETLResult(1, 1)
    }
}