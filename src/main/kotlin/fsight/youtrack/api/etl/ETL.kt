package fsight.youtrack.api.etl

import fsight.youtrack.ETLState
import fsight.youtrack.api.etl.bundles.Bundle
import fsight.youtrack.api.etl.bundles.BundleV2
import fsight.youtrack.api.etl.issues.IIssue
import fsight.youtrack.api.etl.projects.IProjects
import fsight.youtrack.api.etl.timeline.ITimeline
import fsight.youtrack.api.etl.users.UsersETL
import fsight.youtrack.etlV2.issues.IssuesServiceV2
import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.v2.Issue
import org.springframework.stereotype.Service
import java.util.*

@Service
class ETL(
    private val projects: IProjects,
    private val bundles: Bundle,
    private val issue: IIssue,
    private val users: UsersETL,
    private val timeline: ITimeline,
    private val bundleServiceV2: BundleV2,
    private val issuesV2: IssuesServiceV2
) : IETL {

    override fun loadDataFromYT(manual: Boolean, customFilter: String?): ETLResult? {
        println("Current ETL state: ${etlState.state}")
        println("Custom filter: $customFilter")
        when (etlState) {
            ETLState.IDLE, ETLState.DONE -> {
                println("Launching ETL")
                /*etlState = ETLState.RUNNING*/
                val m = GregorianCalendar.getInstance().also { it.time = Date() }.get(Calendar.MINUTE)
                val issuesCount = issue.getIssues(customFilter)
                if (m == 30) {
                    bundles.getBundles()
                    users.getUsers()
                    timeline.launchCalculation()
                    issue.checkIssues()
                    projects.saveProjects()
                }
                etlState = ETLState.IDLE
                lastResult = ETLResult(ETLState.DONE, issuesCount, 0)
            }
            ETLState.RUNNING -> lastResult = ETLResult(ETLState.RUNNING, 0, 0)
        }
        return lastResult
    }

    override fun getBundles() {
        bundleServiceV2.getBundles()
    }

    override fun getUsers() {
        users.getUsers()
    }

    override fun getIssueById(id: String): Issue {
        return issuesV2.getById(id)
    }

    companion object {
        var etlState = ETLState.IDLE
        var lastResult: ETLResult? = null
    }
}