package fsight.youtrack.scheduler

import fsight.youtrack.etl.ETLService
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class ScheduledTasks(private val service: ETLService) : CommandLineRunner {
    private val runOnStartup = false
    @Scheduled(cron = "0 0/10 * * * *")
    fun loadData() {
        service.loadDataFromYT(false, null)
    }

    override fun run(vararg args: String?) {
        if (runOnStartup) service.loadDataFromYT(false, null)
    }
}