package fsight.youtrack.scheduler

import fsight.youtrack.ETLState
import fsight.youtrack.etl.IETL
import fsight.youtrack.etl.IETLState
import fsight.youtrack.integrations.devops.features.IFeaturesAnalyzer
import fsight.youtrack.integrations.devops.revisions.IDevOpsRevisions
import fsight.youtrack.integrations.youtrack.IIssuesAnalyzer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetAddress


@Service
class ScheduledTasks(private val service: IETL, private val state: IETLState) : CommandLineRunner {
    @Autowired
    lateinit var devOpsRevisions: IDevOpsRevisions

    @Autowired
    lateinit var featuresAnalyzer: IFeaturesAnalyzer

    @Autowired
    lateinit var issuesAnalyzer: IIssuesAnalyzer

    private val runOnStartup = false
    private val testServers = listOf(
        "SPB-FSIGHT11",
        "v-hlopind",
        "DESKTOP-62SKE29"
    )

    @Scheduled(cron = "0 0/10 * * * *")
    fun loadData() {
        when {
            state.state == ETLState.RUNNING -> println("ETL is already running")
            InetAddress.getLocalHost().hostName !in testServers && state.state != ETLState.RUNNING -> {
                println("\n*** Scheduled task started ***")
                state.state = ETLState.RUNNING
                val result = service.runScheduledExport()
                state.state = ETLState.IDLE
                println("\n*** Scheduled task finished. Processed ${result?.issues} issues***")
            }
            InetAddress.getLocalHost().hostName !in testServers && state.state == ETLState.RUNNING -> {
                println("\nService is running in production mode, but previous ETL is not finished")
            }
            else -> println("\nService is running in dev mode, ETL will not be launched")
        }
    }

    /**
     * ПН-ПТ в 10 утра
     * */
    @Scheduled(cron = "0 0 10 * * MON-FRI")
    fun notifyProjectOwners() {
        if (InetAddress.getLocalHost().hostName !in testServers) {
            devOpsRevisions.startRevision()
        }
    }

    /**
     * ПН в 10 утра
     * */
    @Scheduled(cron = "0 0 10 * * MON")
    fun analyzeFeatures() {
        if (InetAddress.getLocalHost().hostName !in testServers) {
            featuresAnalyzer.analyzePendingFeatures()
        }
    }

    @Scheduled(cron = "0 0 10 * * MON-FRI")
    fun analyzeCommentedIssues() {
        if (InetAddress.getLocalHost().hostName !in testServers) {
            issuesAnalyzer.analyze()
        }
    }

    override fun run(vararg args: String?) {
        if (runOnStartup) {
            state.state = ETLState.RUNNING
            service.runScheduledExport()
            state.state = ETLState.IDLE
        }
    }
}
