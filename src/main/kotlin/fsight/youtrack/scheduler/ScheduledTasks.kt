package fsight.youtrack.scheduler

import fsight.youtrack.etl.IETL
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.InetAddress


@Component
class ScheduledTasks(private val service: IETL) : CommandLineRunner {
    private val runOnStartup = false
    @Scheduled(cron = "0 0/10 * * * *")
    fun loadData() {
        if (InetAddress.getLocalHost().hostName != "hlopind") service.loadDataFromYT(false, null)
    }

    override fun run(vararg args: String?) {
        if (runOnStartup) service.loadDataFromYT(false, null)
    }
}