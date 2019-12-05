package fsight.youtrack.etl

import fsight.youtrack.ETLState
import fsight.youtrack.etl.bundles.Bundle
import fsight.youtrack.etl.issues.IIssue
import fsight.youtrack.etl.projects.IProjects
import fsight.youtrack.etl.timeline.ITimeline
import fsight.youtrack.etl.users.UsersETL
import fsight.youtrack.models.ETLResult
import fsight.youtrack.models.YouTrackIssue
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
        private val bundle: Bundle
) : IETL {
    private var etlState: ETLState = ETLState.IDLE

    override var state: ETLState
        get() = etlState
        set(value) {
            etlState = value
        }

    override fun loadDataFromYT(manual: Boolean, customFilter: String?, parameters: String): ETLResult? {
        printlnIf(customFilter != null, "Custom filter: $customFilter")
        val p = parameters.split(delimiters = *arrayOf(","))
        val time = GregorianCalendar.getInstance().also { it.time = Date() }
        if (time.get(Calendar.MINUTE) == 30 && time.get(Calendar.HOUR) == 0 && !manual) {
            projects.saveProjects()
            bundle.getBundles()
            users.getUsers()
            issue.findDeletedIssues()
            issue.checkPendingIssues()
            timeline.launchCalculation()
        }
        val issuesCount = when {
            !manual -> issue.getIssues(customFilter)
            manual && p.contains("issues") -> issue.getIssues(customFilter)
            else -> 0
        }
        if (manual) p.forEach {
            when (it) {
                "bundles" -> bundle.getBundles()
                "users" -> users.getUsers()
                "timeline" -> timeline.launchCalculation()
                "check" -> issue.findDeletedIssues()
                "projects" -> projects.saveProjects()
                "pending" -> issue.checkPendingIssues()
            }
        }
        lastResult = ETLResult(state = ETLState.DONE, issues = issuesCount, timeUnit = 0)
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
