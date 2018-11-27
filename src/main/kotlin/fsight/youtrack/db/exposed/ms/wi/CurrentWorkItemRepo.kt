package fsight.youtrack.db.exposed.ms.wi

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CurrentWorkItemRepo(@Qualifier("tfsDataSource") private val ms: Database) {
    fun getCurrentStates(items: List<Int>): List<IssueState> {
        return transaction(ms) {
            CurrentWorkItemTable.select(where = { CurrentWorkItemTable.systemId inList items }).map { row ->
                IssueState(
                    systemId = row[CurrentWorkItemTable.systemId],
                    state = row[CurrentWorkItemTable.state],
                    previousState = row[CurrentWorkItemTable.previousState]
                )
            }
        }
    }

    data class IssueState(
        val systemId: Int,
        val state: String?,
        val previousState: String?
    )
}