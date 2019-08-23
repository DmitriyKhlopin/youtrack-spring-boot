package fsight.youtrack.api.reports

import org.jooq.DSLContext
import org.springframework.stereotype.Service


@Service
class LicensingReport(private val dslContext: DSLContext) : ILicensingReport {
    override fun getData(): Any {
        val licensingQuery = """
    SELECT issues.*,
       b.obj,
       (b.obj ->> 'Assignee')::TEXT                                    AS assignee
FROM issues,
     (SELECT issue_id, json_object_agg(field_name, field_value) AS obj
      FROM custom_field_values fields
      GROUP BY fields.issue_id) b
WHERE issues.id SIMILAR TO '(FMP_LIC|PP_Lic)%'
  AND issues.id = b.issue_id
""".trimIndent()
        println("aaa")
        val i = dslContext.fetch(licensingQuery)
        i.forEach { println(it) }
        return dslContext.fetch(licensingQuery).toString()
        /*return "test"*/
    }
}
