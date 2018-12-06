package fsight.youtrack.db.exposed.pg

import fsight.youtrack.db.exposed.ms.wi.CurrentWorkItemRepo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
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
    fun getActiveIssuesWithClosedTFSIssues(): List<P> {
        val cfvIssue = CustomFieldValuesTable.alias("cfv_issue")
        val cfvIssueField = cfvIssue[CustomFieldValuesTable.fieldValue].alias("tfs_issues")
        val cfvAssignee = CustomFieldValuesTable.alias("cfv_assignee")
        val cfvAssigneeField = cfvAssignee[CustomFieldValuesTable.fieldValue].alias("assignee")
        val ytIssues = arrayListOf<D>()
        transaction(pg) {
            IssuesTable
                .join(
                    cfvIssue,
                    JoinType.LEFT,
                    additionalConstraint = { (IssuesTable.id eq cfvIssue[CustomFieldValuesTable.issueId]) and (cfvIssue[CustomFieldValuesTable.fieldName] eq "Issue") })
                .join(
                    cfvAssignee,
                    JoinType.LEFT,
                    additionalConstraint = { (IssuesTable.id eq cfvAssignee[CustomFieldValuesTable.issueId]) and (cfvAssignee[CustomFieldValuesTable.fieldName] eq "Assignee") }
                )
                .slice(
                    IssuesTable.id,
                    cfvIssueField,
                    IssuesTable.state,
                    cfvAssigneeField
                )
                .select(where = { (IssuesTable.resolvedDateTime.isNull()) and (cfvIssue[CustomFieldValuesTable.fieldValue].isNotNull()) and (IssuesTable.issueType neq "Новая функциональность") })
                .map { row ->
                    try {
                        ytIssues.addAll(
                            (row[cfvIssueField] ?: "")
                                .replace(" ", "")
                                .split(",")
                                .map { item ->
                                    D(
                                        row[IssuesTable.id],
                                        row[cfvAssigneeField] ?: "",
                                        row[IssuesTable.state],
                                        item.toInt()
                                    )
                                }
                        )
                    } catch (e: Exception) {

                    }
                }
        }
        val actualStates = currentWorkItemRepo.getCurrentStates(ytIssues.map { it.tfsIssueId ?: 0 })
        val currentStates = actualStates.map { it.systemId to it.state }.toMap()
        val previousStates = actualStates.map { it.systemId to it.previousState }.toMap()
        ytIssues.forEach { it ->
            it.tfsState = currentStates[it.tfsIssueId]
            it.tfsPreviousState = previousStates[it.tfsIssueId]
        }
        val result =
            ytIssues.filter { j -> j.ytState == "Направлена разработчику" && j.tfsState == "Closed" && ytIssues.filter { i -> i.ytIssueId == j.ytIssueId }.all { it.tfsState == "Closed" } }
        val dist = result.distinctBy { it.ytIssueId }.map { item ->
            P(
                item.ytIssueId,
                item.assignee,
                item.ytState,
                result.filter { r -> r.ytIssueId == item.ytIssueId }
                    .map { e ->
                        println(result)
                        CurrentWorkItemRepo.IssueState(e.tfsIssueId ?: 0, e.tfsPreviousState, e.tfsState)
                    }
            )
        }
        return dist
    }

    data class D(
        val ytIssueId: String,
        val assignee: String,
        val ytState: String,
        var tfsIssueId: Int? = null,
        var tfsState: String? = null,
        var tfsPreviousState: String? = null
    )

    data class P(
        val ytIssueId: String,
        val assignee: String,
        val ytState: String,
        var list: List<CurrentWorkItemRepo.IssueState> = listOf()
    )

    data class IssueError(
        val ytIssueId: String,
        val assignee: String,
        val ytState: String,
        val list: ArrayList<Error> = arrayListOf()
    )

    data class Error(
        val title: String,
        val reason: String
    )
}