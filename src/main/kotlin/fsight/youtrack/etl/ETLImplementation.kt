package fsight.youtrack.etl

import fsight.youtrack.ETLState
import fsight.youtrack.etl.bundles.BundleService
import fsight.youtrack.etl.issues.IssueService
import fsight.youtrack.etl.projects.ProjectsService
import fsight.youtrack.etl.timeline.TimelineService
import fsight.youtrack.etl.users.UsersImplementation
import fsight.youtrack.models.ETLResult
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class ETLImplementation(private val projects: ProjectsService,
                        private val bundles: BundleService,
                        private val issue: IssueService,
                        private val users: UsersImplementation,
                        private val timeline: TimelineService) : ETLService {
    var state = ETLState.IDLE
    var lastResult: ETLResult? = null

    override fun getCurrentState(): ETLState = state

    override fun loadDataFromYT(manual: Boolean): ETLResult? {
        when (state) {
            ETLState.IDLE, ETLState.DONE -> {
                state = ETLState.RUNNING
                val m = GregorianCalendar.getInstance().also { it.time = Date() }.get(Calendar.MINUTE)
                val issuesCount = issue.getIssues()

                if (m == 30) {
                    bundles.getBundles()
                    users.getUsers()
                    timeline.launchCalculation()
                    issue.checkIssues()
                    projects.saveProjects()
                }
                state = ETLState.IDLE
                lastResult = ETLResult(ETLState.DONE, issuesCount, 0)
            }
            ETLState.RUNNING -> lastResult = ETLResult(ETLState.RUNNING, 0, 0)
        }
        return lastResult
    }
}