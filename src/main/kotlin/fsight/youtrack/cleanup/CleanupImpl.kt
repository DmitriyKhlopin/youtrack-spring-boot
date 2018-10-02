package fsight.youtrack.cleanup

import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service
import youtrack.jooq.tables.*

@Service
class CleanupImpl(private val dslContext: DSLContext) {
    /*private val dslContext = dbService.getConnection()*/
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
        dslContext.deleteFrom(Issues.ISSUES).where(Issues.ISSUES.ID.`in`(issues)).execute()
        dslContext.deleteFrom(CustomFieldValues.CUSTOM_FIELD_VALUES).where((CustomFieldValues.CUSTOM_FIELD_VALUES.ISSUE_ID.`in`(issues))).execute()
        dslContext.deleteFrom(IssueComments.ISSUE_COMMENTS).where(IssueComments.ISSUE_COMMENTS.ISSUE_ID.`in`(issues)).execute()
        dslContext.deleteFrom(WorkItems.WORK_ITEMS).where(WorkItems.WORK_ITEMS.ISSUE_ID.`in`(issues)).execute()
        dslContext.deleteFrom(IssueHistory.ISSUE_HISTORY).where(IssueHistory.ISSUE_HISTORY.ISSUE_ID.`in`(issues)).execute()
    }
}