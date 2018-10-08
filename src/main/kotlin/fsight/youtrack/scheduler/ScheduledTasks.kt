package fsight.youtrack.scheduler

import fsight.youtrack.etl.ETLService
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class ScheduledTasks(private val service: ETLService) : CommandLineRunner {
    @Scheduled(cron = "0 0/10 * * * *")
    fun reportCurrentTime() {
        run()
    }

    override fun run(vararg args: String?) {
        service.loadDataFromYT(false)
    }
}