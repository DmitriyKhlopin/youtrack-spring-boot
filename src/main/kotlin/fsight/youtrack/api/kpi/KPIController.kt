package fsight.youtrack.api.kpi

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
            defaultValue = "BIP,CL,FP,INNOWAY,INTER,IT,KRIT,PP,LANIT,RS,RIO,SAPRUN,TEST,SA,TC,BSLT,CBR,BIC,GPI,DRT,GN,P_PROJ1,DSU,INTELL,JET,KORUS,KPMG,LMN,PRESALE,PDP,PO,REA,SIGMA,SPAM,T,TN,NAVICON,P,FS,FSC"
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = "iana.maltseva@fsight.ru,mikhail.nesterov@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,artem.maltsev@fsight,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru"
        ) emails: String,
        @RequestParam("dateFrom", required = false, defaultValue = "2018-08-15") dateFrom: String,
        @RequestParam("dateTo", required = false, defaultValue = "2018-12-31") dateTo: String
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
            defaultValue = "BIP,CL,FP,INNOWAY,INTER,IT,KRIT,PP,LANIT,RS,RIO,SAPRUN,TEST,SA,TC,BSLT,CBR,BIC,GPI,DRT,GN,P_PROJ1,DSU,INTELL,JET,KORUS,KPMG,LMN,PRESALE,PDP,PO,REA,SIGMA,SPAM,T,TN,NAVICON,P,FS,FSC"
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = "iana.maltseva@fsight.ru,mikhail.nesterov@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,artem.maltsev@fsight,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru"
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
            defaultValue = "BIP,CL,FP,INNOWAY,INTER,IT,KRIT,PP,LANIT,RS,RIO,SAPRUN,TEST,SA,TC,BSLT,CBR,BIC,GPI,DRT,GN,P_PROJ1,DSU,INTELL,JET,KORUS,KPMG,LMN,PRESALE,PDP,PO,REA,SIGMA,SPAM,T,TN,NAVICON,P,FS,FSC"
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = "iana.maltseva@fsight.ru,mikhail.nesterov@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,artem.maltsev@fsight,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru"
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
            defaultValue = "BIP,CL,FP,INNOWAY,INTER,IT,KRIT,PP,LANIT,RS,RIO,SAPRUN,TEST,SA,TC,BSLT,CBR,BIC,GPI,DRT,GN,P_PROJ1,DSU,INTELL,JET,KORUS,KPMG,LMN,PRESALE,PDP,PO,REA,SIGMA,SPAM,T,TN,NAVICON,P,FS,FSC"
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = "iana.maltseva@fsight.ru,mikhail.nesterov@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,artem.maltsev@fsight,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru"
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
            defaultValue = "BIP,CL,FP,INNOWAY,INTER,IT,KRIT,PP,LANIT,RS,RIO,SAPRUN,TEST,SA,TC,BSLT,CBR,BIC,GPI,DRT,GN,P_PROJ1,DSU,INTELL,JET,KORUS,KPMG,LMN,PRESALE,PDP,PO,REA,SIGMA,SPAM,T,TN,NAVICON,P,FS,FSC"
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = "iana.maltseva@fsight.ru,mikhail.nesterov@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,artem.maltsev@fsight,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru"
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
            defaultValue = "BIP,CL,FP,INNOWAY,INTER,IT,KRIT,PP,LANIT,RS,RIO,SAPRUN,TEST,SA,TC,BSLT,CBR,BIC,GPI,DRT,GN,P_PROJ1,DSU,INTELL,JET,KORUS,KPMG,LMN,PRESALE,PDP,PO,REA,SIGMA,SPAM,T,TN,NAVICON,P,FS,FSC"
        ) projects: String,
        @RequestParam(
            "emails",
            required = false,
            defaultValue = "iana.maltseva@fsight.ru,mikhail.nesterov@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,artem.maltsev@fsight,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru"
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

