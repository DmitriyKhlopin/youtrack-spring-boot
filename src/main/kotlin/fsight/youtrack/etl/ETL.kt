package fsight.youtrack.etl

import fsight.youtrack.ETLState
import fsight.youtrack.etl.bundles.Bundle
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.etl.projects.IProjects
import fsight.youtrack.etl.timeline.ITimeline
import fsight.youtrack.etl.users.UsersETL
import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.youtrack.Issue
import fsight.youtrack.printlnIf
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ETL(
    private val projects: IProjects,
    private val issue: IIssue,
    private val users: UsersETL,
    private val timeline: ITimeline,
    private val bundle: Bundle,
    private val etlStateService: IETLState
) : IETL {

    override fun runScheduledExport(): ETLResult? {
        val time = GregorianCalendar.getInstance().also { it.time = Date() }
        val issuesCount = if (time.get(Calendar.HOUR) in (5..23)) {
            val ids = issue.getIssues(null)
            issue.getIssuesHistory(ids)
            issue.getWorkItems(ids)
            ids.size
        } else 0
        if (time.get(Calendar.MINUTE) == 30 && time.get(Calendar.HOUR) == 0) {
            projects.saveProjects()
            bundle.getBundles()
            users.getUsers()
            issue.findDeletedIssues()
            issue.checkPendingIssues()
            timeline.launchCalculation()
        }
        lastResult = ETLResult(state = ETLState.DONE, issues = issuesCount, timeUnit = 0)
        return lastResult
    }

    override fun runManualExport(customFilter: String?, parameters: String, dateFrom: String?, dateTo: String?): Any {
        if (etlStateService.state == ETLState.RUNNING) return "ETL is already running"
        printlnIf(customFilter != null, "Custom filter: $customFilter with parameters: $parameters")
        val p = parameters.split(delimiters = *arrayOf(","))
        var ids: ArrayList<String> = arrayListOf()
        p.forEach {
            when (it) {
                "issues" -> ids = issue.getIssues(customFilter)
                "work" -> {
                    if (ids.size == 0) ids = issue.getIssues(customFilter)
                    issue.getWorkItems(ids)
                }
                "history" -> {
                    if (ids.size == 0) ids = issue.getIssues(customFilter)
                    issue.getIssuesHistory(ids)
                }
                "bundles" -> bundle.getBundles()
                "users" -> users.getUsers()
                "timeline" -> timeline.launchCalculation()
                "timelineAll" -> timeline.launchCalculationForPeriod(dateFrom, dateTo)
                "check" -> issue.findDeletedIssues()
                "projects" -> projects.saveProjects()
                "pending" -> issue.checkPendingIssues()
            }
        }

        return ids.size
    }

    override fun getBundles() {
        bundle.getBundles()
    }

    override fun getUsers() {
        users.getUsers()
    }

    override fun getIssueById(id: String): Issue {
        return Issue()
    }

    override fun getIssueHistory(idReadable: String) {
        issue.getSingleIssueHistory(idReadable)
    }

    companion object {
        var lastResult: ETLResult? = null
    }

    override fun getTimelineById(idReadable: String): ResponseEntity<Any> {
        return ResponseEntity.ok(timeline.calculateForId(idReadable, 1, 1))
    }
}
