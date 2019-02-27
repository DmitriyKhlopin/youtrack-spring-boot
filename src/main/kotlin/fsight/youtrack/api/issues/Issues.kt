package fsight.youtrack.api.issues

import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class Issues(private val dslContext: DSLContext, @Qualifier("tfsDataSource") private val ms: Database) : IIssues {
    internal class HighPriorityIssue(
        var id: String? = null,
        var summary: String? = null,
        var created: Timestamp? = null,
        var priority: String? = null,
        var issue: String? = null,
        var tfsIssue: ArrayList<Any>? = null
    )

    override fun getHighPriorityIssuesWithTFSDetails(): Any {
        val priority = CUSTOM_FIELD_VALUES.`as`("priority")
        val issue = CUSTOM_FIELD_VALUES.`as`("issue")
        val type = CUSTOM_FIELD_VALUES.`as`("type")

        return dslContext
            .select(
                ISSUES.ID.`as`("id"),
                ISSUES.SUMMARY.`as`("summary"),
                ISSUES.CREATED_DATE_TIME.`as`("created"),
                priority.FIELD_VALUE.`as`("priority"),
                issue.FIELD_VALUE.`as`("issue")
            )
            .from(ISSUES)
            .leftJoin(priority).on(ISSUES.ID.eq(priority.ISSUE_ID)).and(priority.FIELD_NAME.eq("Priority"))
            .leftJoin(issue).on(ISSUES.ID.eq(issue.ISSUE_ID)).and(issue.FIELD_NAME.eq("Issue"))
            .leftJoin(type).on(ISSUES.ID.eq(type.ISSUE_ID)).and(type.FIELD_NAME.eq("Type"))
            .where(ISSUES.RESOLVED_DATE.isNull)
            .and(priority.FIELD_VALUE.eq("Major"))
            .and(type.FIELD_VALUE.notEqual("Feature"))
            .and(ISSUES.PROJECT_SHORT_NAME.notIn(listOf("SD", "PO", "TC", "W", "PP_Lic")))
            .fetchInto(HighPriorityIssue::class.java)
    }
}
