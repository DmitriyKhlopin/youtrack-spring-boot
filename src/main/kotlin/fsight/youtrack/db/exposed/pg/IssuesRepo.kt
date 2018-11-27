package fsight.youtrack.db.exposed.pg

import fsight.youtrack.db.exposed.ms.wi.CurrentWorkItemRepo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IssuesRepo(
    @Qualifier("pgDataSource") private val pg: Database,
    @Qualifier("msDataSource") private val ms: Database,
    private val currentWorkItemRepo: CurrentWorkItemRepo
) {

    /**
    SELECT
    i.id,
    trim(BOTH ' ' FROM unnest(string_to_array(cfv.field_value, ','))) AS tfs_id
    FROM issues i
    LEFT JOIN custom_field_values cfv ON i.id = cfv.issue_id AND cfv.field_name = 'Issue'
    WHERE i.resolved_date IS NULL AND cfv.field_value IS NOT NULL;
     */
    fun getActiveIssues(): ResponseEntity<Any> {
        val ytIssues = arrayListOf<D>()
        transaction(pg) {
            IssuesTable
                .join(
                    CustomFieldValuesTable,
                    JoinType.LEFT,
                    additionalConstraint = { (IssuesTable.id eq CustomFieldValuesTable.issueId) and (CustomFieldValuesTable.fieldName eq "Issue") }
                )
                .slice(IssuesTable.id, CustomFieldValuesTable.fieldValue, IssuesTable.state)
                .select(where = { (IssuesTable.resolved_date_time.isNull()) and (CustomFieldValuesTable.fieldValue.isNotNull()) })
                .map { row ->
                    try {
                        ytIssues.addAll(row[CustomFieldValuesTable.fieldValue].replace(" ", "").split(",")
                            .map { item -> D(row[IssuesTable.id], item.toInt(), row[IssuesTable.state]) })
                    } catch (e: Exception) {

                    }
                }
        }
        val actualStates = currentWorkItemRepo.getCurrentStates(ytIssues.map { it.tfsIssueId })
        val currentStates = actualStates.map { it.systemId to it.state }.toMap()
        val previousStates = actualStates.map { it.systemId to it.previousState }.toMap()
        ytIssues.forEach { it ->
            it.tfsState = currentStates[it.tfsIssueId]
            it.tfsPreviousState = previousStates[it.tfsIssueId]
        }
        val result = ytIssues.filter { it.ytState == "Направлена разработчику" && it.tfsState == "Closed" }
        return ResponseEntity.ok().body(result)
    }

    data class D(
        val ytIssueId: String,
        /*val responsible: String,*/
        val tfsIssueId: Int,
        val ytState: String,
        var tfsState: String? = null,
        var tfsPreviousState: String? = null
    )
}