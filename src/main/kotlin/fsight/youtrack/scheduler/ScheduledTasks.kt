package fsight.youtrack.scheduler

import fsight.youtrack.etl.ETLService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*


@Component
class ScheduledTasks(
        /*private val projects: ProjectsService,
        private val bundles: BundleService,
        private val issue: IssueService,
        private val users: UsersService,
        private val timeline: IssuesTimelineService*/
        private val service: ETLService
) : CommandLineRunner {
    var isRunning = false
    private val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    @Scheduled(cron = "0 0/10 * * * *")
    fun reportCurrentTime() {
        log.info("The time is now ${dateFormat.format(Date())}", dateFormat.format(Date()))
        if (!isRunning) run()
    }

    override fun run(vararg args: String?) {
        val h = GregorianCalendar.getInstance().also { it.time = Date() }.get(Calendar.HOUR_OF_DAY)
        isRunning = true
        service.loadDataFromYT(false)
        /*projects.getProjects()
        if (h in listOf(8, 20)) bundles.getBundles()
        issue.getIssues()
        users.getUsers()
        timeline.start()
        if (h in listOf(8, 20)) issue.checkIssues()*/
        isRunning = false
    }
}