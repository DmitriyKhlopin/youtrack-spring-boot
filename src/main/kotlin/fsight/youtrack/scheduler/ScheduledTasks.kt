package fsight.youtrack.scheduler

import fsight.youtrack.etl.ETLService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat


@Component
class ScheduledTasks(
        /*private val projects: ProjectsService,
        private val bundles: BundleService,
        private val issue: IssueService,
        private val users: UsersService,
        private val timeline: IssuesTimelineService*/
        private val service: ETLService
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    @Scheduled(cron = "0 0/10 * * * *")
    fun reportCurrentTime() {
        run()
    }

    override fun run(vararg args: String?) {
        service.loadDataFromYT(false)
    }
}