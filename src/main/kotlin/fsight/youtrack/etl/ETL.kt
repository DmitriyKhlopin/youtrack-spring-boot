package fsight.youtrack.etl

import fsight.youtrack.ETLState
import fsight.youtrack.etl.bundles.Bundle
import fsight.youtrack.etl.bundles.BundleV2
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.etl.projects.IProjects
import fsight.youtrack.etl.timeline.ITimeline
import fsight.youtrack.etl.users.UsersETL
import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.v2.Issue
import org.springframework.stereotype.Service
import java.util.*

@Service
class ETL(
    private val projects: IProjects,
    /*private val bundles: Bundle,*/
    private val issue: IIssue,
    private val users: UsersETL,
    private val timeline: ITimeline,
    private val bundleServiceV2: BundleV2
) : IETL {
    override fun loadDataFromYT(manual: Boolean, customFilter: String?, parameters: String): ETLResult? {
        println("Current ETL state: ${etlState.state}")
        println("Custom filter: $customFilter")
        val p = parameters.split(delimiters = *arrayOf(","))
        when (etlState) {
            ETLState.IDLE, ETLState.DONE -> {
                println("Launching ETL")
                /*etlState = ETLState.RUNNING*/
                val m = GregorianCalendar.getInstance().also { it.time = Date() }.get(Calendar.MINUTE)
                var issuesCount = 0
                if (m == 30 && !manual) {
                    issuesCount = issue.getIssues(customFilter)
                    /*bundles.getBundles()*/
                    bundleServiceV2.getBundles()
                    users.getUsers()
                    timeline.launchCalculation()
                    issue.checkIssues()
                    projects.saveProjects()
                } else {
                    if (!manual) issuesCount = issue.getIssues(customFilter)
                }
                if (manual && p.contains("issues")) {
                    println("Loading issues")
                    issuesCount = issue.getIssues(customFilter)
                }
                if (manual && p.contains("bundles")) {
                    println("Loading bundles")
                    bundleServiceV2.getBundles()
                    /*bundles.getBundles()*/
                }
                if (manual && p.contains("users")) {
                    println("Loading users")
                    users.getUsers()
                }
                if (manual && p.contains("timeline")) {
                    println("Calculating timelines")
                    timeline.launchCalculation()
                }
                if (manual && p.contains("check")) {
                    println("Checking issues")
                    issue.checkIssues()
                }
                if (manual && p.contains("projects")) {
                    println("Loading projects")
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
        return Issue()
    }

    companion object {
        var etlState = ETLState.IDLE
        var lastResult: ETLResult? = null
    }
}
