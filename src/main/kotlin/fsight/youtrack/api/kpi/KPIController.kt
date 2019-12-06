package fsight.youtrack.api.kpi

import fsight.youtrack.api.dictionaries.IDictionary
import fsight.youtrack.defaultUsers
import fsight.youtrack.splitToList
import fsight.youtrack.toEndOfDate
import fsight.youtrack.toStartOfDate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class KPIController(private val service: IKPI, private val dictionaries: IDictionary) {

    @GetMapping("/api/kpi")
    fun getResult(
            @RequestParam("projects", required = false) projects: String?,
            @RequestParam("emails", required = false, defaultValue = defaultUsers) emails: String,
            @RequestParam("dateFrom", required = false, defaultValue = "2019-01-01") dateFrom: String,
            @RequestParam("dateTo", required = false, defaultValue = "2019-06-30") dateTo: String,
            @RequestParam("mode", required = false, defaultValue = "default") mode: String,
            @RequestParam("withDetails", required = false, defaultValue = "false") withDetails: Boolean
    ): Any {
        val df = dateFrom.toStartOfDate()
        val dt = dateTo.toEndOfDate()
        val p = projects?.splitToList() ?: dictionaries.commercialProjects
        val u = emails.splitToList()
        return when (mode) {
            "result" -> service.getResult(p, u, df, dt, withDetails)
            "overall" -> service.getOverallResult(p, df, dt)
            else -> service.getResult(p, u, df, dt, withDetails)
        }
    }

}

