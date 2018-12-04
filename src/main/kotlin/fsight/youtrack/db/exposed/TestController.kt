package fsight.youtrack.db.exposed

import fsight.youtrack.db.exposed.ms.schedule.fact.WorkHoursRepo
import fsight.youtrack.db.exposed.ms.schedule.plan.ScheduleTimeIntervalRepo
import fsight.youtrack.db.exposed.pg.IssuesRepo
import fsight.youtrack.db.exposed.pg.ProjectDictionaryRepo
import fsight.youtrack.db.exposed.pg.TimeAccountingExtendedRepo
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@CrossOrigin
@RestController
class TestController(
    private val scheduleTimeIntervalRepo: ScheduleTimeIntervalRepo,
    private val projectDictionaryRepo: ProjectDictionaryRepo,
    private val workHoursRepo: WorkHoursRepo,
    private val issuesRepo: IssuesRepo,
    private val timeAccountingExtendedRepo: TimeAccountingExtendedRepo
) {
    @GetMapping("/api/project_test")
    fun getProjects() = projectDictionaryRepo.get()

    @GetMapping("/api/schedule_test")
    fun getSchedules() = scheduleTimeIntervalRepo.getTopTen()

    @GetMapping("/api/fact_hours_test")
    fun getFactHours() = workHoursRepo.getWorkHours()

    @GetMapping("/api/active_issues_test")
    fun getActiveIssues() = issuesRepo.getActiveIssuesWithClosedTFSIssues()

    @GetMapping("/api/time_accounting_extended_test")
    fun getAll() = timeAccountingExtendedRepo.getAll()

    @GetMapping("/api/time_accounting_extended_test_grouped")
    fun getGroupedByProjectType() = timeAccountingExtendedRepo.getGroupedByProjectType("", "", "")
}