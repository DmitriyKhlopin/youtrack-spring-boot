package fsight.youtrack.etl

import fsight.youtrack.ETLState
import fsight.youtrack.etl.bundles.Bundle
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.etl.projects.IProjects
import fsight.youtrack.etl.timeline.ITimeline
import fsight.youtrack.etl.users.UsersETL
import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.YouTrackIssue
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ETL(
    private val projects: IProjects,
    private val issue: IIssue,
    private val users: UsersETL,
    private val timeline: ITimeline,
    private val bundle: Bundle
) : IETL {
    private var etlState: ETLState = ETLState.IDLE

    override var state: ETLState
        get() = etlState
        set(value) {
            etlState = value
        }

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
                    projects.saveProjects()
                    bundle.getBundles()
                    users.getUsers()
                    issue.findDeletedIssues()
                    issue.checkPendingIssues()
                    timeline.launchCalculation()
                } else {
                    if (!manual) issuesCount = issue.getIssues(customFilter)
                }
                if (manual && p.contains("issues")) {
                    println("Loading issues only")
                    issuesCount = issue.getIssues(customFilter)
                }
                if (manual && p.contains("bundles")) {
                    println("Loading bundles")
                    bundle.getBundles()
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
                    issue.findDeletedIssues()
                }
                if (manual && p.contains("projects")) {
                    println("Loading projects")
                    projects.saveProjects()
                }
                if (manual && p.contains("pending")) {
                    println("Checking pending issues")
                    issue.checkPendingIssues()
                }
                etlState = ETLState.IDLE
                lastResult = ETLResult(ETLState.DONE, issuesCount, 0)
            }
            ETLState.RUNNING -> lastResult = ETLResult(ETLState.RUNNING, 0, 0)
        }
        /*etlState = ETLState.IDLE*/
        return lastResult
    }

    override fun getBundles() {
        bundle.getBundles()
    }

    override fun getUsers() {
        users.getUsers()
    }

    override fun getIssueById(id: String): YouTrackIssue {
        return YouTrackIssue()
    }

    override fun getIssueHistory(idReadable: String) {
        issue.getIssueHistory(idReadable)
    }

    companion object {
        var etlState = ETLState.IDLE
        var lastResult: ETLResult? = null
    }

    override fun getTimelineById(idReadable: String): ResponseEntity<Any> {
        return ResponseEntity.ok(timeline.calculateForId(idReadable))
    }
}
