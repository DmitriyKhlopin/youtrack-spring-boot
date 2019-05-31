package fsight.youtrack.api.kpi

import fsight.youtrack.defaultProjects
import fsight.youtrack.defaultUsers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RestController
class KPIController(private val service: IKPI) {

    @GetMapping("/api/kpi")
    fun getResult(
        @RequestParam(
            "projects",
            required = false,
            defaultValue = defaultProjects
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = defaultUsers
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2019-01-01") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2019-06-30") dateTo: String
    ): List<Any> {
        val df = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())
        val dt = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))
        val p = projects.removeSurrounding("[", "]").split(",")
        val u = emails.removeSurrounding("[", "]").split(",")
        return service.getResult(p, u, df, dt)
    }

    @GetMapping("/api/kpi/total")
    fun getTotal(
        @RequestParam(
            "projects",
            required = false,
            defaultValue = defaultProjects
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = defaultUsers
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2018-08-15") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2018-12-31") dateTo: String
    ): List<Any> {
        val df = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())
        val dt = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))
        val p = projects.removeSurrounding("[", "]").split(",")
        val u = emails.removeSurrounding("[", "]").split(",")
        return service.getTotal(p, u, df, dt)
    }

    @GetMapping("/api/kpi/violations")
    fun getViolations(
        @RequestParam(
            "projects",
            required = false,
            defaultValue = defaultProjects
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = defaultUsers
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2018-08-15") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2018-12-31") dateTo: String
    ): List<Any> {
        val df = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())
        val dt = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))
        val p = projects.removeSurrounding("[", "]").split(",")
        val u = emails.removeSurrounding("[", "]").split(",")
        return service.getViolations(p, u, df, dt)
    }

    @GetMapping("/api/kpi/postponement")
    fun getPostponement(
        @RequestParam(
            "projects",
            required = false,
            defaultValue = defaultProjects
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = defaultUsers
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2018-08-15") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2018-12-31") dateTo: String
    ): List<Any> {
        val df = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())
        val dt = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))
        val p = projects.removeSurrounding("[", "]").split(",")
        val u = emails.removeSurrounding("[", "]").split(",")
        return service.getPostponements(p, u, df, dt)
    }

    @GetMapping("/api/kpi/suggestions")
    fun getSuggestedSolutions(
        @RequestParam(
            "projects",
            required = false,
            defaultValue = defaultProjects
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = defaultUsers
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2018-08-15") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2018-12-31") dateTo: String
    ): List<Any> {
        val df = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())
        val dt = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))
        val p = projects.removeSurrounding("[", "]").split(",")
        val u = emails.removeSurrounding("[", "]").split(",")
        return service.getSuggestedSolutions(p, u, df, dt)
    }

    @GetMapping("/api/kpi/clarifications")
    fun getRequestedClarifications(
        @RequestParam(
            "projects",
            required = false,
            defaultValue = defaultProjects
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = defaultUsers
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2018-08-15") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2018-12-31") dateTo: String
    ): List<Any> {
        val df = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay())
        val dt = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59))
        val p = projects.removeSurrounding("[", "]").split(",")
        val u = emails.removeSurrounding("[", "]").split(",")
        return service.getRequestedClarifications(p, u, df, dt)
    }
}

