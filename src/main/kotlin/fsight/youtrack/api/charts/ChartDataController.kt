package fsight.youtrack.api.charts

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class ChartDataController(

    private val timeAccountingExtendedRepo: TimeAccountingExtendedRepo
) {
    @Autowired
    lateinit var service: ChartData

    @PostMapping("/api/chart/dynamics")
    fun getTimeLineData(@RequestBody issueFilter: IssueFilter) = service.getDynamicsData(issueFilter)

    @PostMapping("/api/chart/sigma")
    fun getSigmaData(
        @RequestBody issueFilter: IssueFilter
    ) = service.getSigmaData(issueFilter)

    @PostMapping("/api/chart/created_on_week")
    fun getCreatedCountOnWeek(
        @RequestBody issueFilter: IssueFilter
    ) = service.getCreatedCountOnWeek(issueFilter)

    @GetMapping("/api/chart/time_accounting_extended_grouped")
    fun getGroupedTimeUnitsByProjectType(
        @RequestParam("projects", required = false) projects: String = "",
        @RequestParam("dateFrom", required = false) dateFrom: String = "",
        @RequestParam("dateTo", required = false) dateTo: String = ""
    ) = timeAccountingExtendedRepo.getGroupedByProjectType(projects, dateFrom, dateTo)


    @GetMapping("/api/chart/processed")
    fun getProcessedDaily(
        @RequestParam("projects", required = false) projects: String = "",
        @RequestParam("dateFrom", required = false) dateFrom: String = "",
        @RequestParam("dateTo", required = false) dateTo: String = ""
    ): Any {
        return service.getProcessedDaily(projects, dateFrom, dateTo)
    }

    @PostMapping("/api/chart/priorities")
    fun getPrioritiesStats(
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getPrioritiesStats(issueFilter))
    }

    @PostMapping("/api/chart/types")
    fun getTypesStats(
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getTypesStats(issueFilter))
    }

    @PostMapping("/api/chart/avg_lifetime")
    fun getAverageLifetime(
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getAverageLifetime(issueFilter))
    }

    @PostMapping("/api/chart/avg_lifetime_unresolved")
    fun getAverageLifetimeUnresolved(
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getAverageLifetimeUnresolved(issueFilter))
    }

    @PostMapping("/api/chart/sla_violations")
    fun getSLAStatsByPriority(
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getSLAStatsByPriority(issueFilter))
    }

    @PostMapping("/api/chart/commercial_sla_violations")
    fun getCommercialSLAStatsByPriority(
        @RequestBody issueFilter: IssueFilter
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.getCommercialSLAStatsByPriority(issueFilter))
    }

    @PostMapping("/api/chart/velocity")
    fun getVelocity(@RequestBody issueFilter: IssueFilter): Any {
        return service.getVelocity(issueFilter)
    }
}
