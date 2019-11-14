package fsight.youtrack.scheduler

import fsight.youtrack.ETLState
import fsight.youtrack.etl.IETL
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetAddress


@Service
class ScheduledTasks(private val service: IETL) : CommandLineRunner {
    private val runOnStartup = false
    @Scheduled(cron = "0 0/10 * * * *")
    fun loadData() {
        when {
            InetAddress.getLocalHost().hostName != "hlopind" && service.state == ETLState.IDLE -> {
                println("*** Scheduled task started ***")
                service.state = ETLState.RUNNING
                val result = service.loadDataFromYT(false, null)
                service.state = ETLState.IDLE
                println("*** Scheduled task finished. Processed ${result?.issues} issues***")
            }
            InetAddress.getLocalHost().hostName != "hlopind" && service.state == ETLState.RUNNING -> {
                println("Service is running in production mode, but previous ETL is not finished")
            }
            else -> println("Service is running in dev mode, ETL will not be launched")
        }
    }

    override fun run(vararg args: String?) {
        if (runOnStartup) {
            service.state = ETLState.RUNNING
            service.loadDataFromYT(false, null)
            service.state = ETLState.IDLE
        }
    }
}
