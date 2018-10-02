package fsight.youtrack.scheduler

import fsight.youtrack.bundles.BundleService
import fsight.youtrack.issues.IssueService
import fsight.youtrack.projects.ProjectsService
import fsight.youtrack.timeline.IssuesTimelineService
import fsight.youtrack.users.UsersService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*


@Component
class ScheduledTasks(
        private val projects: ProjectsService,
        private val bundles: BundleService,
        private val issue: IssueService,
        private val users: UsersService,
        private val timeline: IssuesTimelineService
) : CommandLineRunner {
    private var isRunning = false
    private val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    @Scheduled(cron = "0 0/10 * * * *")
    fun reportCurrentTime() {
        log.info("The time is now ${dateFormat.format(Date())}", dateFormat.format(Date()))
        if (!isRunning) run()
    }

    override fun run(vararg args: String?) {
        isRunning = true
        projects.getProjects()
        bundles.getBundles()
        issue.getIssues()
        users.getUsers()
        timeline.start()
        issue.checkIssues()
        isRunning = false
    }
}