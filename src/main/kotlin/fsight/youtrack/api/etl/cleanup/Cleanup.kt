package fsight.youtrack.api.etl.cleanup

import fsight.youtrack.generated.jooq.tables.CustomFieldValues
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.IssueComments.ISSUE_COMMENTS
import fsight.youtrack.generated.jooq.tables.IssueHistory.ISSUE_HISTORY
import fsight.youtrack.generated.jooq.tables.Issues
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.generated.jooq.tables.WorkItems.WORK_ITEMS
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service

@Service
class Cleanup(private val dslContext: DSLContext) {
    fun deleteAllFromDatabase() {
        try {
            /*dslContext.deleteFrom(ISSUES).execute()*/
            /*dslContext.deleteFrom(CUSTOM_FIELD_VALUES).execute()*/
            /*dslContext.deleteFrom(BUNDLES).execute()*/
            /*dslContext.deleteFrom(ISSUE_COMMENTS).execute()*/
            /*dslContext.deleteFrom(WORK_ITEMS).execute()*/
            /*dslContext.deleteFrom(ISSUE_HISTORY).execute()*/
            /*dslContext.deleteFrom(USERS).execute()*/
            /*dslContext.deleteFrom(PROJECTS).execute()*/

        } catch (e: DataAccessException) {
            println(e.message)
        }
    }

    fun deleteIssueFromDatabase(issues: ArrayList<String>) {
        dslContext.deleteFrom(ISSUES).where(Issues.ISSUES.ID.`in`(issues)).execute()
        dslContext.deleteFrom(CUSTOM_FIELD_VALUES).where((CustomFieldValues.CUSTOM_FIELD_VALUES.ISSUE_ID.`in`(issues)))
            .execute()
        dslContext.deleteFrom(ISSUE_COMMENTS).where(ISSUE_COMMENTS.ISSUE_ID.`in`(issues)).execute()
        dslContext.deleteFrom(WORK_ITEMS).where(WORK_ITEMS.ISSUE_ID.`in`(issues)).execute()
        dslContext.deleteFrom(ISSUE_HISTORY).where(ISSUE_HISTORY.ISSUE_ID.`in`(issues)).execute()
    }
}